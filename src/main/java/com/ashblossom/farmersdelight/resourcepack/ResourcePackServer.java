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

        String host = plugin.getConfig().getString("resource-pack-host", "").trim();
        int port = plugin.getServer().getPort();

        if (host.isEmpty()) {
            plugin.getLogger().warning("resource-pack-host is not set in config.yml — resource pack won't be sent to players.");
            plugin.getLogger().warning("Set it to your server's external IP, e.g.: resource-pack-host: \"136.175.187.108\"");
            return;
        }

        packUrl = "http://" + host + ":" + port + "/pack.zip";
        plugin.getLogger().info("Resource pack URL: " + packUrl);
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
                plugin.getLogger().info("Pack built: " + texFound + " textures included, " + texMissing + " missing.");
            }
            packBytes = bos.toByteArray();
            packHash  = MessageDigest.getInstance("SHA-1").digest(packBytes);
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

        JsonObject root = new JsonObject();
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

    public String getUrl()        { return packUrl; }
    public byte[] getHash()       { return packHash; }
    public byte[] getPackBytes()  { return packBytes; }
}
