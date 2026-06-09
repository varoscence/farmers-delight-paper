package com.ashblossom.farmersdelight.blocks;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.gui.RecipeViewerGui;
import com.ashblossom.farmersdelight.items.FDItems;
import com.ashblossom.farmersdelight.recipes.CookingRecipe;
import com.ashblossom.farmersdelight.recipes.FDRecipes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.Base64;

public class CookingPotManager {

    private static final int[] INGREDIENT_SLOTS = {0, 1, 2, 3, 4};
    private static final int OUTPUT_SLOT = 11;
    private static final int CONTAINER_SLOT = 12;
    private static final int RECIPE_BOOK_SLOT = 26;
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
        boolean isNew = !openGuis.containsKey(k);
        Inventory inv = openGuis.computeIfAbsent(k, x ->
            plugin.getServer().createInventory(null, 27,
                Component.text("Cooking Pot", NamedTextColor.DARK_RED)));

        // Restore inventory on first open this session.
        if (isNew) {
            ItemStack[] saved = guiContents.get(k);
            if (saved != null) {
                // In-memory copy from this session
                ItemStack[] restored = new ItemStack[27];
                for (int s : INGREDIENT_SLOTS) if (s < saved.length) restored[s] = saved[s];
                if (OUTPUT_SLOT < saved.length) restored[OUTPUT_SLOT] = saved[OUTPUT_SLOT];
                if (CONTAINER_SLOT < saved.length) restored[CONTAINER_SLOT] = saved[CONTAINER_SLOT];
                inv.setContents(restored);
            } else {
                // Persisted from previous session — load from storage
                loadContentsFromStorage(loc, inv);
            }
        }

        // Overlay decorative glass and recipe book button (always refresh these)
        ItemStack gray = makeGray();
        for (int i = 5; i <= 10; i++) inv.setItem(i, gray);
        for (int i = 13; i <= 25; i++) inv.setItem(i, gray);
        inv.setItem(RECIPE_BOOK_SLOT, makeRecipeBookButton());

        player.openInventory(inv);
    }

    public void handleGuiClick(Player player, InventoryClickEvent event, Location potLoc) {
        if (event.getClickedInventory() == null) return;
        Inventory potInv = event.getView().getTopInventory();
        // Only intercept clicks in the cooking pot side (not player's own inventory)
        if (!potInv.equals(event.getClickedInventory())) return;

        int slot = event.getSlot();

        // Recipe book button
        if (slot == RECIPE_BOOK_SLOT) {
            event.setCancelled(true);
            plugin.getServer().getScheduler().runTask(plugin, () ->
                RecipeViewerGui.openCooking(player, 0));
            return;
        }

        // Check if this is a functional slot
        boolean isIngredient = false;
        for (int s : INGREDIENT_SLOTS) if (s == slot) { isIngredient = true; break; }

        if (!isIngredient && slot != OUTPUT_SLOT && slot != CONTAINER_SLOT) {
            event.setCancelled(true);
            return;
        }

        // Output slot: block placing, allow taking
        if (slot == OUTPUT_SLOT) {
            InventoryAction action = event.getAction();
            if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE
                    || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
                event.setCancelled(true);
                return;
            }
            // Taking from output — schedule ingredient consumption on next tick
            ItemStack out = potInv.getItem(OUTPUT_SLOT);
            if (out != null && out.getType() != Material.AIR) {
                plugin.getServer().getScheduler().runTask(plugin, () ->
                    onOutputTaken(player, potLoc, potInv));
            }
            return;
        }

        // Container slot: block placing, allow taking
        if (slot == CONTAINER_SLOT) {
            InventoryAction action = event.getAction();
            if (action == InventoryAction.PLACE_ALL || action == InventoryAction.PLACE_ONE
                    || action == InventoryAction.PLACE_SOME || action == InventoryAction.SWAP_WITH_CURSOR) {
                event.setCancelled(true);
            }
        }

        // Ingredient slots: allow all interactions
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
        guiContents.put(key(potLoc), functionalContents(inv));
    }

    public void onClose(Location potLoc, Inventory inv) {
        ItemStack[] contents = functionalContents(inv);
        guiContents.put(key(potLoc), contents);
        saveContentsToStorage(potLoc, contents); // survive server restart
    }

    public void saveAll() {
        // Also flush any open pot inventories before shutdown
        for (Map.Entry<String, Inventory> e : openGuis.entrySet()) {
            String k = e.getKey();
            String[] parts = k.split(":");
            World world = plugin.getServer().getWorld(parts[0]);
            if (world == null) continue;
            Location loc = new Location(world,
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                Integer.parseInt(parts[3]));
            saveContentsToStorage(loc, functionalContents(e.getValue()));
        }
        storage.save();
    }

    private ItemStack[] functionalContents(Inventory inv) {
        ItemStack[] all = inv.getContents();
        ItemStack[] out = new ItemStack[27];
        for (int s : INGREDIENT_SLOTS) out[s] = all[s];
        out[OUTPUT_SLOT] = all[OUTPUT_SLOT];
        out[CONTAINER_SLOT] = all[CONTAINER_SLOT];
        return out;
    }

    private ItemStack makeGray() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.displayName(Component.empty());
        pane.setItemMeta(meta);
        return pane;
    }

    private ItemStack makeRecipeBookButton() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("View Cooking Recipes").color(NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(Component.text("Click to browse all recipes.")
            .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
        item.setItemMeta(meta);
        return item;
    }

    // ── Persistence helpers ───────────────────────────────────────────────────

    private void saveContentsToStorage(Location loc, ItemStack[] contents) {
        int[] slots = {0, 1, 2, 3, 4, OUTPUT_SLOT, CONTAINER_SLOT};
        for (int s : slots) {
            ItemStack item = (s < contents.length) ? contents[s] : null;
            String key = "slot_" + s;
            if (item == null || item.getType() == Material.AIR) {
                storage.remove(loc, key);
            } else {
                String encoded = serializeItem(item);
                if (encoded != null) storage.set(loc, key, encoded);
            }
        }
    }

    private void loadContentsFromStorage(Location loc, Inventory inv) {
        int[] slots = {0, 1, 2, 3, 4, OUTPUT_SLOT, CONTAINER_SLOT};
        for (int s : slots) {
            String encoded = storage.get(loc, "slot_" + s);
            if (encoded != null) {
                ItemStack item = deserializeItem(encoded);
                if (item != null) inv.setItem(s, item);
            }
        }
    }

    private String serializeItem(ItemStack item) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(bos)) {
                byte[] bytes = item.serializeAsBytes();
                dos.writeInt(bytes.length);
                dos.write(bytes);
            }
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) { return null; }
    }

    private ItemStack deserializeItem(String data) {
        try {
            byte[] raw = Base64.getDecoder().decode(data);
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(raw))) {
                int len = dis.readInt();
                byte[] itemBytes = new byte[len];
                dis.readFully(itemBytes);
                return ItemStack.deserializeBytes(itemBytes);
            }
        } catch (Exception e) { return null; }
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
