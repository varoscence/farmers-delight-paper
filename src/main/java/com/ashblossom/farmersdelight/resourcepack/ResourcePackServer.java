package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.bukkit.Material;

public class ResourcePackServer {

    private final FarmersDelightPlugin plugin;
    private HttpServer server;
    private byte[] packBytes;
    private byte[] packHash = new byte[0];
    private String packUrl;

    public ResourcePackServer(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        String externalUrl = plugin.getConfig().getString("resource-pack-url", "").trim();

        if (!externalUrl.isEmpty()) {
            packUrl = externalUrl;
            String sha1Url = externalUrl.replaceAll("pack\\.zip$", "pack.sha1");
            try {
                java.net.URLConnection conn = new java.net.URI(sha1Url).toURL().openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (InputStream in = conn.getInputStream()) {
                    String hex = new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
                    packHash = hexToBytes(hex);
                    plugin.getLogger().info("Resource pack SHA1 loaded: " + hex);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not fetch pack SHA1 from " + sha1Url + ": " + e.getMessage());
                plugin.getLogger().warning("Players will be prompted without hash verification.");
            }
            plugin.getLogger().info("Using external resource pack: " + packUrl);
            return;
        }

        // Self-hosted fallback
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
                addResource(zip, "pack.mcmeta", "pack/pack.mcmeta");

                // Model files (standard parent/textures format)
                for (FDItems item : FDItems.values()) {
                    addString(zip, "assets/farmersdelight/models/item/" + item.getId() + ".json",
                        buildModelFile(item));
                }

                // Item definition files — one per base material, all with CMD dispatch
                Map<Material, List<FDItems>> byMaterial = new LinkedHashMap<>();
                for (FDItems item : FDItems.values()) {
                    byMaterial.computeIfAbsent(item.getBaseMaterial(), k -> new ArrayList<>()).add(item);
                }
                for (Map.Entry<Material, List<FDItems>> entry : byMaterial.entrySet()) {
                    String name = entry.getKey().name().toLowerCase();
                    String defJson = buildItemDefinition(entry.getKey(), entry.getValue());
                    if (defJson == null) continue; // all items for this material lack textures
                    addString(zip, "assets/minecraft/items/" + name + ".json", defJson);
                }

                // Textures
                for (FDItems item : FDItems.values()) {
                    InputStream tex = plugin.getResource("textures/item/" + item.getId() + ".png");
                    if (tex != null) {
                        zip.putNextEntry(new ZipEntry("assets/farmersdelight/textures/item/" + item.getId() + ".png"));
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
        }
    }

    private String buildModelFile(FDItems item) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "farmersdelight:item/" + item.getId());
        root.add("textures", textures);
        return new Gson().toJson(root);
    }

    private String buildItemDefinition(Material material, List<FDItems> items) {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:select");
        model.addProperty("property", "minecraft:custom_model_data");

        JsonArray cases = new JsonArray();
        for (FDItems item : items) {
            // Skip items with no texture — they'd show purple/black error texture
            InputStream texCheck = plugin.getResource("textures/item/" + item.getId() + ".png");
            if (texCheck == null) continue;
            try { texCheck.close(); } catch (IOException ignored) {}

            JsonObject c = new JsonObject();
            c.addProperty("when", item.getCmd());
            JsonObject mdl = new JsonObject();
            mdl.addProperty("type", "minecraft:model");
            mdl.addProperty("model", "farmersdelight:item/" + item.getId());
            c.add("model", mdl);
            cases.add(c);
        }
        if (cases.isEmpty()) return null;
        model.add("cases", cases);

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", "minecraft:item/" + material.name().toLowerCase());
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

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public String getUrl() { return packUrl; }
    public byte[] getHash() { return packHash; }
}
