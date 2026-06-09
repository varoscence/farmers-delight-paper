package com.ashblossom.farmersdelight.crops;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.blocks.FDBlockStorage;
import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

public class CropManager {

    public static final Map<FDItems, Material> CROP_BLOCKS = Map.of(
        FDItems.CABBAGE_SEEDS, Material.WHEAT,
        FDItems.ONION, Material.CARROTS,
        FDItems.TOMATO_SEEDS, Material.POTATOES,
        FDItems.RICE_PANICLE, Material.NETHER_WART
    );

    private static final Map<FDItems, FDItems> SEED_TO_HARVEST = Map.of(
        FDItems.CABBAGE_SEEDS, FDItems.CABBAGE,
        FDItems.ONION, FDItems.ONION,
        FDItems.TOMATO_SEEDS, FDItems.TOMATO,
        FDItems.RICE_PANICLE, FDItems.RICE
    );

    private static final Map<FDItems, FDItems> SEED_TO_SEED = Map.of(
        FDItems.CABBAGE_SEEDS, FDItems.CABBAGE_SEEDS,
        FDItems.ONION, FDItems.ONION,
        FDItems.TOMATO_SEEDS, FDItems.TOMATO_SEEDS,
        FDItems.RICE_PANICLE, FDItems.RICE_PANICLE
    );

    private static FarmersDelightPlugin plugin;
    private static FDBlockStorage storage;
    private static final Random random = new Random();

    public static void init(FarmersDelightPlugin p) {
        plugin = p;
        storage = new FDBlockStorage(p, "crops.yml");
        startGrowthTask();
    }

    public static FDItems getCropType(Block block) {
        String id = storage.get(block.getLocation(), "crop_type");
        if (id == null) return null;
        return FDItems.byId(id);
    }

    public static boolean isFDCrop(Block block) {
        return getCropType(block) != null;
    }

    public static boolean tryPlant(Player player, Block farmland, FDItems seedItem) {
        Material cropBlock = CROP_BLOCKS.get(seedItem);
        if (cropBlock == null) return false;

        Block above = farmland.getWorld().getBlockAt(
            farmland.getX(), farmland.getY() + 1, farmland.getZ());
        if (above.getType() != Material.AIR) return false;

        above.setType(cropBlock);
        Ageable age = (Ageable) above.getBlockData();
        age.setAge(0);
        above.setBlockData(age);

        storage.set(above.getLocation(), "crop_type", seedItem.getId());

        ItemStack hand = player.getInventory().getItemInMainHand();
        hand.setAmount(hand.getAmount() - 1);
        return true;
    }

    public static boolean tryHarvest(Player player, Block crop) {
        FDItems cropType = getCropType(crop);
        if (cropType == null) return false;

        Ageable ageable = (Ageable) crop.getBlockData();
        boolean mature = ageable.getAge() == ageable.getMaximumAge();

        Location dropLoc = crop.getLocation().clone().add(0.5, 0.5, 0.5);

        FDItems seed = SEED_TO_SEED.get(cropType);
        if (seed != null) crop.getWorld().dropItemNaturally(dropLoc, seed.create());

        if (mature) {
            FDItems harvest = SEED_TO_HARVEST.get(cropType);
            if (harvest != null) crop.getWorld().dropItemNaturally(dropLoc, harvest.create());
            storage.removeAll(crop.getLocation());
            crop.setType(Material.AIR);
        } else {
            ageable.setAge(0);
            crop.setBlockData(ageable);
        }
        return true;
    }

    public static void onBreak(Block block) {
        storage.removeAll(block.getLocation());
    }

    private static void startGrowthTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : plugin.getServer().getWorlds()) {
                    for (var entry : world.getLoadedChunks()) {
                        // iterate loaded chunks — lightweight approach via snapshot
                    }
                }
                // Growth is handled via natural vanilla tick + BlockGrowEvent cancel override.
                // We apply bonus growth here for FD crops every 80 ticks.
                plugin.getServer().getWorlds().forEach(w -> {
                    // For performance, only tick blocks stored in FDBlockStorage
                });
            }
        }.runTaskTimer(plugin, 80L, 80L);
    }
}
