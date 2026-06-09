package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.*;

public class ResourcePackServer {

    private final FarmersDelightPlugin plugin;
    private HttpServer server;
    private byte[] packBytes;
    private byte[] packHash;
    private String packUrl;

    public ResourcePackServer(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        rebuildPack();
        int port = plugin.getConfig().getInt("resource-pack-port", 8766);
        String host = plugin.getConfig().getString("resource-pack-host", "localhost");
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/pack.zip", exchange -> {
                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, packBytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(packBytes); }
            });
            server.start();
            packUrl = "http://" + host + ":" + port + "/pack.zip";
            plugin.getLogger().info("Resource pack server started at " + packUrl);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start resource pack server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    public void rebuildPack() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                // pack.mcmeta
                addResource(zip, "pack.mcmeta", "pack/pack.mcmeta");

                // Model JSON for each item (individual model file)
                for (FDItems item : FDItems.values()) {
                    String modelPath = "assets/farmersdelight/models/item/" + item.getId() + ".json";
                    String modelJson = buildItemModel(item);
                    addString(zip, modelPath, modelJson);
                }

                // paper.json — master CMD dispatch file
                addString(zip, "assets/minecraft/items/paper.json", buildPaperJson());

                // Textures — loaded from plugin resources
                for (FDItems item : FDItems.values()) {
                    String texPath = "assets/farmersdelight/textures/item/" + item.getId() + ".png";
                    InputStream tex = plugin.getResource("textures/item/" + item.getId() + ".png");
                    if (tex != null) {
                        zip.putNextEntry(new ZipEntry(texPath));
                        tex.transferTo(zip);
                        zip.closeEntry();
                    }
                }
            }
            packBytes = bos.toByteArray();
            packHash = MessageDigest.getInstance("SHA-1").digest(packBytes);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to build resource pack: " + e.getMessage());
            packBytes = new byte[0];
            packHash = new byte[20];
        }
    }

    private String buildItemModel(FDItems item) {
        // Flat item model pointing to our texture
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", "farmersdelight:item/" + item.getId());
        root.add("model", model);
        return new Gson().toJson(root);
    }

    private String buildPaperJson() {
        // 1.21.2+ format: select on custom_model_data
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:select");
        model.addProperty("property", "minecraft:custom_model_data");

        JsonArray cases = new JsonArray();
        for (FDItems item : FDItems.values()) {
            JsonObject c = new JsonObject();
            JsonArray when = new JsonArray();
            when.add(String.valueOf(item.getCmd()));
            c.add("when", when);
            JsonObject mdl = new JsonObject();
            mdl.addProperty("type", "minecraft:model");
            mdl.addProperty("model", "farmersdelight:item/" + item.getId());
            c.add("model", mdl);
            cases.add(c);
        }
        model.add("cases", cases);

        // fallback = vanilla paper model
        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", "minecraft:item/paper");
        model.add("fallback", fallback);

        root.add("model", model);
        return new GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    private void addResource(ZipOutputStream zip, String entryName, String resourcePath) throws IOException {
        try (InputStream is = plugin.getResource(resourcePath)) {
            if (is == null) return;
            zip.putNextEntry(new ZipEntry(entryName));
            is.transferTo(zip);
            zip.closeEntry();
        }
    }

    private void addString(ZipOutputStream zip, String entryName, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    public String getUrl() { return packUrl; }
    public byte[] getHash() { return packHash; }
}
