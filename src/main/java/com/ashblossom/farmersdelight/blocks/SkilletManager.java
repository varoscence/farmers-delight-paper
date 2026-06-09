package com.ashblossom.farmersdelight.blocks;

import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SkilletManager {

    public record SkilletRecipe(Object input, ItemStack output) {
        public boolean matches(ItemStack item) {
            if (item == null || item.getType() == Material.AIR) return false;
            if (input instanceof FDItems fd) return fd == FDItems.fromStack(item);
            if (input instanceof Material mat) return item.getType() == mat;
            return false;
        }
    }

    private static final Set<Material> HEAT_SOURCES = Set.of(
        Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
        Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
        Material.MAGMA_BLOCK
    );

    private final List<SkilletRecipe> recipes = new ArrayList<>();

    public SkilletManager() {
        // Eggs & dairy
        add(Material.EGG,              FDItems.FRIED_EGG);
        // Meat cuts
        add(FDItems.CHICKEN_CUTS,      FDItems.COOKED_CHICKEN_CUTS);
        add(FDItems.BACON,             FDItems.COOKED_BACON);
        add(FDItems.MUTTON_CHOPS,      FDItems.COOKED_MUTTON_CHOPS);
        add(FDItems.MINCED_BEEF,       FDItems.BEEF_PATTY);
        // Fish
        add(FDItems.COD_SLICE,         FDItems.COOKED_COD_SLICE);
        add(FDItems.SALMON_SLICE,      FDItems.COOKED_SALMON_SLICE);
    }

    private void add(FDItems input, FDItems output) {
        recipes.add(new SkilletRecipe(input, output.create()));
    }

    private void add(Material input, FDItems output) {
        recipes.add(new SkilletRecipe(input, output.create()));
    }

    public boolean isHeatSource(Block block) {
        return HEAT_SOURCES.contains(block.getType());
    }

    public boolean tryFry(Player player, Block heatSource) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        SkilletRecipe recipe = null;
        for (SkilletRecipe r : recipes) {
            if (r.matches(offhand)) { recipe = r; break; }
        }
        if (recipe == null) return false;

        // Consume one ingredient from offhand
        if (offhand.getAmount() > 1) offhand.setAmount(offhand.getAmount() - 1);
        else player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));

        // Give result
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(recipe.output().clone());
        for (ItemStack drop : leftover.values())
            heatSource.getWorld().dropItemNaturally(player.getLocation(), drop);

        // Sizzle
        heatSource.getWorld().playSound(
            heatSource.getLocation().add(0.5, 0.5, 0.5),
            Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.8f);
        heatSource.getWorld().spawnParticle(
            org.bukkit.Particle.SMOKE,
            heatSource.getLocation().add(0.5, 1.1, 0.5), 6, 0.1, 0.05, 0.1, 0.01);

        player.sendActionBar(
            net.kyori.adventure.text.Component.text("Sizzle! ")
                .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                .append(recipe.output().displayName()));
        return true;
    }
}
