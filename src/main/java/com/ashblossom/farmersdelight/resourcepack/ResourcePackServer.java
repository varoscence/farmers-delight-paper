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
import java.util.concurrent.Executors;
import java.util.zip.*;
import org.bukkit.Material;

public class ResourcePackServer {

    private final FarmersDelightPlugin plugin;
    private volatile byte[] packBytes = new byte[0];
    private volatile byte[] packHash  = new byte[0];
    private String packUrl;
    private HttpServer httpServer;

    public ResourcePackServer(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        rebuildPack();

        String host = plugin.getConfig().getString("resource-pack-host", "").trim();
        if (host.isEmpty()) {
            plugin.getLogger().severe("[FD] resource-pack-host is not set in config.yml!");
            return;
        }

        int httpPort = plugin.getConfig().getInt("resource-pack-port", 28083);

        try {
            httpServer = HttpServer.create(new InetSocketAddress(httpPort), 0);
            httpServer.createContext("/pack.zip", exchange -> {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                    return;
                }
                byte[] data = packBytes;
                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, data.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(data);
                }
                plugin.getLogger().info("[FD] Served resource pack: " + data.length + " bytes");
            });
            httpServer.setExecutor(Executors.newCachedThreadPool());
            httpServer.start();

            packUrl = "http://" + host + ":" + httpPort + "/pack.zip";
            plugin.getLogger().info("[FD] Pack HTTP server started on port " + httpPort);
            plugin.getLogger().info("[FD] Pack URL: " + packUrl + " (" + packBytes.length + " bytes)");
        } catch (IOException e) {
            plugin.getLogger().severe("[FD] Could not start HTTP server on port " + httpPort + ": " + e.getMessage());
        }
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
    }

    public void rebuildPack() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                addResource(zip, "pack.mcmeta", "pack/pack.mcmeta");

                for (FDItems item : FDItems.values()) {
                    addString(zip,
                        "assets/farmersdelight/models/item/" + item.getId() + ".json",
                        buildModelJson(item));
                }

                Map<Material, List<FDItems>> byMat = new LinkedHashMap<>();
                for (FDItems item : FDItems.values())
                    byMat.computeIfAbsent(item.getBaseMaterial(), k -> new ArrayList<>()).add(item);

                for (Map.Entry<Material, List<FDItems>> e : byMat.entrySet()) {
                    String json = buildModelOverridesJson(e.getKey(), e.getValue());
                    if (json != null)
                        addString(zip, "assets/minecraft/models/item/" + e.getKey().name().toLowerCase() + ".json", json);
                }

                int found = 0, missing = 0;
                for (FDItems item : FDItems.values()) {
                    String path = "textures/item/" + item.getId() + ".png";
                    try (InputStream tex = plugin.getResource(path)) {
                        if (tex != null) {
                            zip.putNextEntry(new ZipEntry("assets/farmersdelight/textures/item/" + item.getId() + ".png"));
                            tex.transferTo(zip);
                            zip.closeEntry();
                            found++;
                        } else {
                            missing++;
                        }
                    }
                }
                plugin.getLogger().info("[FD] Pack: " + found + " textures bundled, " + missing + " missing.");
            }
            packBytes = bos.toByteArray();
            packHash  = MessageDigest.getInstance("SHA-1").digest(packBytes);
        } catch (Exception e) {
            plugin.getLogger().severe("[FD] Failed to build resource pack: " + e.getMessage());
        }
    }

    // ── JSON builders ────────────────────────────────────────────────────────

    private String buildModelJson(FDItems item) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject tex = new JsonObject();
        tex.addProperty("layer0", "farmersdelight:item/" + item.getId());
        root.add("textures", tex);
        return new Gson().toJson(root);
    }

    private String buildModelOverridesJson(Material mat, List<FDItems> items) {
        JsonArray overrides = new JsonArray();
        for (FDItems item : items) {
            try (InputStream check = plugin.getResource("textures/item/" + item.getId() + ".png")) {
                if (check == null) continue;
            } catch (IOException ignored) { continue; }

            JsonObject predicate = new JsonObject();
            predicate.addProperty("custom_model_data", item.getCmd());
            JsonObject override = new JsonObject();
            override.add("predicate", predicate);
            override.addProperty("model", "farmersdelight:item/" + item.getId());
            overrides.add(override);
        }
        if (overrides.isEmpty()) return null;

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/" + mat.name().toLowerCase());

        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        root.add("textures", textures);
        root.add("overrides", overrides);
        return new GsonBuilder().setPrettyPrinting().create().toJson(root);
    }

    // ── ZIP helpers ──────────────────────────────────────────────────────────

    private void addResource(ZipOutputStream zip, String entry, String resourcePath) throws IOException {
        try (InputStream is = plugin.getResource(resourcePath)) {
            if (is == null) return;
            zip.putNextEntry(new ZipEntry(entry));
            is.transferTo(zip);
            zip.closeEntry();
        }
    }

    private void addString(ZipOutputStream zip, String entry, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(entry));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String getUrl()       { return packUrl; }
    public byte[] getHash()      { return packHash; }
    public byte[] getPackBytes() { return packBytes; }
    public int getHttpPort()     { return plugin.getConfig().getInt("resource-pack-port", 28083); }
}
