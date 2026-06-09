package com.ashblossom.farmersdelight.blocks;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FDBlockStorage {
    private final FarmersDelightPlugin plugin;
    private final File file;
    private final ConcurrentHashMap<String, Map<String, String>> data = new ConcurrentHashMap<>();

    public FDBlockStorage(FarmersDelightPlugin plugin, String filename) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), filename);
        plugin.getDataFolder().mkdirs();
        load();
    }

    public void set(Location loc, String key, String value) {
        data.computeIfAbsent(key(loc), k -> new HashMap<>()).put(key, value);
        saveAsync();
    }
    public String get(Location loc, String key) {
        Map<String, String> e = data.get(key(loc));
        return e != null ? e.get(key) : null;
    }
    public boolean has(Location loc, String key) { return get(loc, key) != null; }
    public void remove(Location loc, String key) {
        Map<String, String> e = data.get(key(loc));
        if (e == null) return;
        e.remove(key);
        if (e.isEmpty()) data.remove(key(loc));
        saveAsync();
    }
    public void removeAll(Location loc) { data.remove(key(loc)); saveAsync(); }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String locKey : yaml.getKeys(false)) {
            var section = yaml.getConfigurationSection(locKey);
            if (section == null) continue;
            Map<String, String> entry = new HashMap<>();
            for (String k : section.getKeys(false)) entry.put(k, section.getString(k, ""));
            data.put(locKey, entry);
        }
    }
    public void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (var outer : data.entrySet())
            for (var inner : outer.getValue().entrySet())
                yaml.set(outer.getKey() + "." + inner.getKey(), inner.getValue());
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().severe("FDBlockStorage save failed: " + e.getMessage()); }
    }
    private void saveAsync() { plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::save); }
    private String key(Location loc) { return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ(); }
}
