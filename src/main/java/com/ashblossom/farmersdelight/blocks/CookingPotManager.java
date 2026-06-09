package com.ashblossom.farmersdelight.blocks;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import com.ashblossom.farmersdelight.recipes.CookingRecipe;
import com.ashblossom.farmersdelight.recipes.FDRecipes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CookingPotManager {

    private static final int[] INGREDIENT_SLOTS = {0, 1, 2, 3, 4};
    private static final int OUTPUT_SLOT = 11;
    private static final int CONTAINER_SLOT = 12;
    private static final String PROGRESS_KEY = "cook_progress";
    private static final String RECIPE_KEY = "recipe_idx";

    private final FarmersDelightPlugin plugin;
    private final FDBlockStorage storage;
    private final Map<String, Inventory> openGuis = new HashMap<>();
    private final Map<String, ItemStack[]> guiContents = new HashMap<>();

    public CookingPotManager(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
        this.storage = new FDBlockStorage(plugin, "cooking_pots.yml");
        startCookingTask();
    }

    private String key(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public boolean isCookingPot(Block block) {
        return block.getType() == Material.CAULDRON && storage.has(block.getLocation(), "type");
    }

    public void placePot(Location loc) {
        storage.set(loc, "type", "cooking_pot");
    }

    public void breakPot(Location loc) {
        storage.removeAll(loc);
        openGuis.remove(key(loc));
        guiContents.remove(key(loc));
    }

    public void openPot(Player player, Location loc) {
        String k = key(loc);
        Inventory inv = openGuis.computeIfAbsent(k, x ->
            plugin.getServer().createInventory(null, 27,
                Component.text("Cooking Pot", NamedTextColor.DARK_RED)));
        ItemStack[] contents = guiContents.get(k);
        if (contents != null) inv.setContents(contents);
        player.openInventory(inv);
    }

    public void handleGuiClick(Player player, InventoryClickEvent event, Location potLoc) {
        if (event.getSlot() == OUTPUT_SLOT
            && (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)) {
            event.setCancelled(true);
        }
    }

    public void onOutputTaken(Player player, Location potLoc, Inventory inv) {
        String idxStr = storage.get(potLoc, RECIPE_KEY);
        if (idxStr == null) return;
        int idx = Integer.parseInt(idxStr);
        if (idx < 0 || idx >= FDRecipes.COOKING.size()) return;
        CookingRecipe recipe = FDRecipes.COOKING.get(idx);

        List<Object> needed = new ArrayList<>(recipe.getIngredients());
        for (int slot : INGREDIENT_SLOTS) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType() == Material.AIR) continue;
            Iterator<Object> it = needed.iterator();
            while (it.hasNext()) {
                Object req = it.next();
                boolean match = (req instanceof FDItems fd && fd == FDItems.fromStack(stack))
                    || (req instanceof Material mat && stack.getType() == mat);
                if (match) {
                    stack.setAmount(stack.getAmount() - 1);
                    if (stack.getAmount() <= 0) inv.setItem(slot, new ItemStack(Material.AIR));
                    it.remove();
                    break;
                }
            }
        }

        if (recipe.getContainer() != Material.AIR) {
            inv.setItem(CONTAINER_SLOT, new ItemStack(recipe.getContainer()));
        }

        storage.remove(potLoc, RECIPE_KEY);
        storage.remove(potLoc, PROGRESS_KEY);
        inv.setItem(OUTPUT_SLOT, new ItemStack(Material.AIR));
        guiContents.put(key(potLoc), inv.getContents());
    }

    public void onClose(Location potLoc, Inventory inv) {
        guiContents.put(key(potLoc), inv.getContents());
    }

    public void saveAll() {
        storage.save();
    }

    private boolean isHeated(Location potLoc) {
        Block below = potLoc.getWorld().getBlockAt(
            potLoc.getBlockX(), potLoc.getBlockY() - 1, potLoc.getBlockZ());
        Material m = below.getType();
        return m == Material.CAMPFIRE || m == Material.SOUL_CAMPFIRE
            || m == Material.BLAST_FURNACE || m == Material.FURNACE || m == Material.SMOKER;
    }

    private void startCookingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Inventory> entry : new HashMap<>(openGuis).entrySet()) {
                    String k = entry.getKey();
                    Inventory inv = entry.getValue();
                    String[] parts = k.split(":");
                    World world = plugin.getServer().getWorld(parts[0]);
                    if (world == null) continue;
                    Location loc = new Location(world,
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        Integer.parseInt(parts[3]));
                    if (!isHeated(loc)) continue;
                    ItemStack outSlot = inv.getItem(OUTPUT_SLOT);
                    if (outSlot != null && outSlot.getType() != Material.AIR) continue;

                    List<ItemStack> current = new ArrayList<>();
                    for (int slot : INGREDIENT_SLOTS) {
                        ItemStack s = inv.getItem(slot);
                        if (s != null && s.getType() != Material.AIR) current.add(s);
                    }
                    if (current.isEmpty()) continue;

                    int matchIdx = -1;
                    for (int i = 0; i < FDRecipes.COOKING.size(); i++) {
                        if (FDRecipes.COOKING.get(i).matches(current)) { matchIdx = i; break; }
                    }
                    if (matchIdx < 0) continue;

                    CookingRecipe recipe = FDRecipes.COOKING.get(matchIdx);
                    String progressStr = storage.get(loc, PROGRESS_KEY);
                    int progress = progressStr != null ? Integer.parseInt(progressStr) : 0;
                    progress += 20;
                    if (progress >= recipe.getCookTime()) {
                        inv.setItem(OUTPUT_SLOT, recipe.getOutput());
                        storage.set(loc, RECIPE_KEY, String.valueOf(matchIdx));
                        storage.remove(loc, PROGRESS_KEY);
                    } else {
                        storage.set(loc, PROGRESS_KEY, String.valueOf(progress));
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
