package com.ashblossom.farmersdelight.resourcepack;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;
import org.bukkit.Material;

public class ResourcePackServer {

    private final FarmersDelightPlugin plugin;
    private volatile byte[] packBytes = new byte[0];
    private volatile byte[] packHash  = new byte[0];
    private String packUrl;

    public ResourcePackServer(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        rebuildPack();

        // Primary: GitHub Pages (or any explicit URL set in config).
        // This is the reliable path — the pack is also bundled in the JAR
        // so the Netty injector can serve it on the game port as a bonus,
        // but the URL sent to players always points here.
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
                    plugin.getLogger().info("Resource pack SHA1: " + hex);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Could not fetch pack SHA1: " + e.getMessage() +
                    " — players will be prompted without hash verification.");
                packHash = new byte[0];
            }
            plugin.getLogger().info("Resource pack URL: " + packUrl);
            return;
        }

        // Fallback: serve from the game port via Netty injection.
        // Requires resource-pack-host to be set to the server's external IP.
        String host = plugin.getConfig().getString("resource-pack-host", "").trim();
        if (!host.isEmpty()) {
            int port = plugin.getServer().getPort();
            packUrl  = "http://" + host + ":" + port + "/pack.zip";
            packHash = MessageDigest_sha1(packBytes);
            plugin.getLogger().info("Resource pack will be served on game port: " + packUrl);
        } else {
            plugin.getLogger().warning("No resource-pack-url or resource-pack-host set in config.yml — pack will not be sent to players.");
        }
    }

    public void stop() {}

    public void rebuildPack() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                addResource(zip, "pack.mcmeta", "pack/pack.mcmeta");

                for (FDItems item : FDItems.values()) {
                    addString(zip, "assets/farmersdelight/models/item/" + item.getId() + ".json",
                        buildModelFile(item));
                }

                Map<Material, List<FDItems>> byMaterial = new LinkedHashMap<>();
                for (FDItems item : FDItems.values()) {
                    byMaterial.computeIfAbsent(item.getBaseMaterial(), k -> new ArrayList<>()).add(item);
                }
                for (Map.Entry<Material, List<FDItems>> entry : byMaterial.entrySet()) {
                    String name = entry.getKey().name().toLowerCase();
                    String defJson = buildItemDefinition(entry.getKey(), entry.getValue());
                    if (defJson == null) continue;
                    addString(zip, "assets/minecraft/items/" + name + ".json", defJson);
                }

                int texFound = 0, texMissing = 0;
                for (FDItems item : FDItems.values()) {
                    InputStream tex = plugin.getResource("textures/item/" + item.getId() + ".png");
                    if (tex != null) {
                        zip.putNextEntry(new ZipEntry("assets/farmersdelight/textures/item/" + item.getId() + ".png"));
                        tex.transferTo(zip);
                        zip.closeEntry();
                        texFound++;
                    } else {
                        texMissing++;
                    }
                }
                plugin.getLogger().info("Pack built: " + texFound + " textures, " + texMissing + " missing.");
            }
            packBytes = bos.toByteArray();
            plugin.getLogger().info("Pack size: " + packBytes.length + " bytes");
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
        JsonArray cases = new JsonArray();
        for (FDItems item : items) {
            InputStream tex = plugin.getResource("textures/item/" + item.getId() + ".png");
            if (tex == null) continue;
            try { tex.close(); } catch (IOException ignored) {}

            JsonObject c = new JsonObject();
            c.addProperty("when", item.getCmd());
            JsonObject mdl = new JsonObject();
            mdl.addProperty("type", "minecraft:model");
            mdl.addProperty("model", "farmersdelight:item/" + item.getId());
            c.add("model", mdl);
            cases.add(c);
        }
        if (cases.isEmpty()) return null;

        JsonObject root  = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:select");
        model.addProperty("property", "minecraft:custom_model_data");
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

    private static byte[] MessageDigest_sha1(byte[] data) {
        try { return MessageDigest.getInstance("SHA-1").digest(data); }
        catch (Exception e) { return new byte[0]; }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            out[i/2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i+1), 16));
        return out;
    }

    public String getUrl()       { return packUrl; }
    public byte[] getHash()      { return packHash; }
    public byte[] getPackBytes() { return packBytes; }
}
