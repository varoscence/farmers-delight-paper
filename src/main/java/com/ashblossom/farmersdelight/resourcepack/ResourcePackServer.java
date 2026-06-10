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
        rebuildPack(); // textures are bundled in the JAR — always available

        String host = plugin.getConfig().getString("resource-pack-host", "").trim();
        if (host.isEmpty()) {
            plugin.getLogger().severe("[FD] resource-pack-host is not set in config.yml!");
            plugin.getLogger().severe("[FD] Edit plugins/FarmersDelight/config.yml and set it to your server IP.");
            return;
        }
        int port = plugin.getServer().getPort();
        packUrl = "http://" + host + ":" + port + "/pack.zip";
        plugin.getLogger().info("[FD] Pack URL: " + packUrl + "  (" + packBytes.length + " bytes)");
    }

    public void stop() {}

    public void rebuildPack() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(bos)) {
                addResource(zip, "pack.mcmeta", "pack/pack.mcmeta");

                // One model file per item (flat sprite using bundled texture)
                for (FDItems item : FDItems.values()) {
                    addString(zip,
                        "assets/farmersdelight/models/item/" + item.getId() + ".json",
                        buildModelJson(item));
                }

                // One item-definition file per base material (CMD dispatch)
                Map<Material, List<FDItems>> byMat = new LinkedHashMap<>();
                for (FDItems item : FDItems.values())
                    byMat.computeIfAbsent(item.getBaseMaterial(), k -> new ArrayList<>()).add(item);

                for (Map.Entry<Material, List<FDItems>> e : byMat.entrySet()) {
                    // New format (1.21.4+ clients): assets/minecraft/items/
                    String json = buildItemDefinitionJson(e.getKey(), e.getValue());
                    if (json != null)
                        addString(zip, "assets/minecraft/items/" + e.getKey().name().toLowerCase() + ".json", json);
                    // Old format (1.21.1 clients): assets/minecraft/models/item/ with predicate overrides
                    String legacyJson = buildModelOverridesJson(e.getKey(), e.getValue());
                    if (legacyJson != null)
                        addString(zip, "assets/minecraft/models/item/" + e.getKey().name().toLowerCase() + ".json", legacyJson);
                }

                // Textures — all bundled inside the JAR under textures/item/
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
            // Write pack to disk so it can be manually inspected or loaded
            try {
                java.io.File out = new java.io.File(plugin.getDataFolder(), "debug_pack.zip");
                plugin.getDataFolder().mkdirs();
                java.nio.file.Files.write(out.toPath(), packBytes);
                plugin.getLogger().info("[FD] Debug pack written to " + out.getAbsolutePath());
            } catch (Exception ex) {
                plugin.getLogger().warning("[FD] Could not write debug pack: " + ex.getMessage());
            }
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

    private String buildItemDefinitionJson(Material mat, List<FDItems> items) {
        JsonArray cases = new JsonArray();
        for (FDItems item : items) {
            // Only add dispatch case if the texture is actually in the JAR
            try (InputStream check = plugin.getResource("textures/item/" + item.getId() + ".png")) {
                if (check == null) continue;
            } catch (IOException ignored) { continue; }

            JsonObject c = new JsonObject();
            c.addProperty("when", item.getCmd());
            JsonObject mdl = new JsonObject();
            mdl.addProperty("type", "minecraft:model");
            mdl.addProperty("model", "farmersdelight:item/" + item.getId());
            c.add("model", mdl);
            cases.add(c);
        }
        if (cases.isEmpty()) return null;

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", "minecraft:item/" + mat.name().toLowerCase());

        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:select");
        model.addProperty("property", "minecraft:custom_model_data");
        model.add("cases", cases);
        model.add("fallback", fallback);

        JsonObject root = new JsonObject();
        root.add("model", model);
        return new GsonBuilder().setPrettyPrinting().create().toJson(root);
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

        String matName = mat.name().toLowerCase();
        String parent = "minecraft:item/generated";

        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "minecraft:item/" + matName);

        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);
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
}
