package com.ashblossom.farmersdelight.blocks;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import com.ashblossom.farmersdelight.recipes.CuttingRecipe;
import com.ashblossom.farmersdelight.recipes.FDRecipes;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class CuttingBoardManager {

    private static final NamespacedKey CB_DISPLAY_KEY = new NamespacedKey("farmersdelight", "cb_display");
    private static final NamespacedKey CB_TYPE_KEY = new NamespacedKey("farmersdelight", "cb_type");

    private final FarmersDelightPlugin plugin;
    private final FDBlockStorage storage;
    private final Random random = new Random();

    public CuttingBoardManager(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
        this.storage = new FDBlockStorage(plugin, "cutting_boards.yml");
    }

    public boolean isCuttingBoard(Block block) {
        if (block.getType() != Material.OAK_SLAB) return false;
        if (!(block.getBlockData() instanceof Slab slab)) return false;
        if (slab.getType() != Slab.Type.BOTTOM) return false;
        return storage.has(block.getLocation(), "type");
    }

    public void placeBoard(Location loc) {
        storage.set(loc, "type", "cutting_board");
    }

    public void breakBoard(Location loc) {
        String itemData = storage.get(loc, "item");
        if (itemData != null) {
            ItemStack item = deserialize(itemData);
            if (item != null) loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 1, 0.5), item);
        }
        removeDisplay(loc);
        storage.removeAll(loc);
    }

    public boolean hasItem(Location loc) {
        return storage.has(loc, "item");
    }

    public boolean placeItem(Location loc, ItemStack item) {
        storage.set(loc, "item", serialize(item));
        spawnDisplay(loc, item);
        return true;
    }

    public void tryCut(Location loc, Player player) {
        String itemData = storage.get(loc, "item");
        if (itemData == null) return;
        ItemStack item = deserialize(itemData);
        if (item == null) return;

        CuttingRecipe recipe = null;
        for (CuttingRecipe r : FDRecipes.CUTTING) {
            if (r.matches(item)) { recipe = r; break; }
        }
        if (recipe == null) return;

        Location dropLoc = loc.clone().add(0.5, 1.1, 0.5);
        for (CuttingRecipe.ChanceOutput out : recipe.getOutputs()) {
            if (out.chance() >= 1.0f || random.nextFloat() < out.chance()) {
                loc.getWorld().dropItemNaturally(dropLoc, out.item().clone());
            }
        }

        removeDisplay(loc);
        storage.remove(loc, "item");
        loc.getWorld().playSound(loc.clone().add(0.5, 0, 0.5), Sound.BLOCK_WOOD_HIT, 1f, 1.2f);
    }

    private void spawnDisplay(Location loc, ItemStack item) {
        removeDisplay(loc);
        Location displayLoc = loc.clone().add(0.5, 0.75, 0.5);
        ItemDisplay display = (ItemDisplay) loc.getWorld().spawnEntity(displayLoc, EntityType.ITEM_DISPLAY);
        display.setItemStack(item.clone());
        display.getPersistentDataContainer().set(CB_DISPLAY_KEY, PersistentDataType.BYTE, (byte) 1);
        display.setTransformation(new Transformation(
            new Vector3f(0, 0, 0),
            new AxisAngle4f((float) Math.toRadians(90), 1, 0, 0),
            new Vector3f(0.45f, 0.45f, 0.45f),
            new AxisAngle4f(0, 0, 1, 0)));
    }

    private void removeDisplay(Location loc) {
        for (Entity e : loc.getWorld().getNearbyEntities(loc.clone().add(0.5, 0.75, 0.5), 0.5, 0.5, 0.5)) {
            if (e instanceof ItemDisplay id
                && id.getPersistentDataContainer().has(CB_DISPLAY_KEY)) {
                id.remove();
            }
        }
    }

    private String serialize(ItemStack item) {
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

    private ItemStack deserialize(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes))) {
                int len = dis.readInt();
                byte[] itemBytes = new byte[len];
                dis.readFully(itemBytes);
                return ItemStack.deserializeBytes(itemBytes);
            }
        } catch (Exception e) { return null; }
    }
}
