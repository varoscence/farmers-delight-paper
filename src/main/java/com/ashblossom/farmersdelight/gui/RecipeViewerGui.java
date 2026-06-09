package com.ashblossom.farmersdelight.gui;

import com.ashblossom.farmersdelight.items.FDItems;
import com.ashblossom.farmersdelight.recipes.CookingRecipe;
import com.ashblossom.farmersdelight.recipes.CuttingRecipe;
import com.ashblossom.farmersdelight.recipes.FDRecipes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RecipeViewerGui {

    private static final int RECIPES_PER_PAGE = 45;
    private static final int BACK_SLOT = 45;
    private static final int PREV_SLOT = 48;
    private static final int PAGE_SLOT = 49;
    private static final int NEXT_SLOT = 50;

    public static void openCooking(Player player, int page) {
        List<CookingRecipe> all = FDRecipes.COOKING;
        int pages = Math.max(1, (int) Math.ceil(all.size() / (double) RECIPES_PER_PAGE));
        page = Math.max(0, Math.min(page, pages - 1));

        Inventory inv = Bukkit.createInventory(
            new FDInventoryHolder(FDInventoryHolder.GuiType.COOKING_RECIPES, page),
            54,
            Component.text("Cooking Pot Recipes").color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        );

        int start = page * RECIPES_PER_PAGE;
        for (int i = 0; i < RECIPES_PER_PAGE && (start + i) < all.size(); i++) {
            inv.setItem(i, buildCookingItem(all.get(start + i)));
        }
        fillNav(inv, page, pages);
        player.openInventory(inv);
    }

    public static void openCutting(Player player, int page) {
        List<CuttingRecipe> all = FDRecipes.CUTTING;
        int pages = Math.max(1, (int) Math.ceil(all.size() / (double) RECIPES_PER_PAGE));
        page = Math.max(0, Math.min(page, pages - 1));

        Inventory inv = Bukkit.createInventory(
            new FDInventoryHolder(FDInventoryHolder.GuiType.CUTTING_RECIPES, page),
            54,
            Component.text("Cutting Board Recipes").color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        );

        int start = page * RECIPES_PER_PAGE;
        for (int i = 0; i < RECIPES_PER_PAGE && (start + i) < all.size(); i++) {
            inv.setItem(i, buildCuttingItem(all.get(start + i)));
        }
        fillNav(inv, page, pages);
        player.openInventory(inv);
    }

    private static ItemStack buildCookingItem(CookingRecipe recipe) {
        ItemStack display = recipe.getOutput().clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Ingredients:").color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        for (Object ing : recipe.getIngredients()) {
            lore.add(Component.text("  · " + ingName(ing)).color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        }
        lore.add(Component.empty());
        lore.add(Component.text("Cook time: " + (recipe.getCookTime() / 20) + "s")
            .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        if (recipe.getContainer() != null && recipe.getContainer() != Material.AIR) {
            lore.add(Component.text("Returns: " + matName(recipe.getContainer()))
                .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private static ItemStack buildCuttingItem(CuttingRecipe recipe) {
        ItemStack display = ingToStack(recipe.getInput()).clone();
        ItemMeta meta = display.getItemMeta();
        if (meta == null) return display;

        if (!meta.hasDisplayName()) {
            meta.displayName(Component.text(ingName(recipe.getInput())).color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Cuts into:").color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        for (CuttingRecipe.ChanceOutput co : recipe.getOutputs()) {
            String name = stackName(co.item());
            String amt = co.item().getAmount() > 1 ? co.item().getAmount() + "x " : "";
            String chance = co.chance() < 1.0f ? String.format(" (%.0f%%)", co.chance() * 100) : "";
            lore.add(Component.text("  · " + amt + name + chance).color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        display.setItemMeta(meta);
        return display;
    }

    private static void fillNav(Inventory inv, int page, int pages) {
        ItemStack gray = GuideBookGui.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 45; i < 54; i++) inv.setItem(i, gray);

        inv.setItem(BACK_SLOT, GuideBookGui.button(Material.RED_STAINED_GLASS_PANE,
            "← Back to Guide", NamedTextColor.RED));

        if (page > 0) {
            inv.setItem(PREV_SLOT, GuideBookGui.button(Material.ARROW,
                "◄ Previous", NamedTextColor.WHITE, "Page " + page));
        }

        ItemStack pageItem = new ItemStack(Material.PAPER);
        ItemMeta m = pageItem.getItemMeta();
        m.displayName(Component.text("Page " + (page + 1) + " / " + pages)
            .color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        pageItem.setItemMeta(m);
        inv.setItem(PAGE_SLOT, pageItem);

        if (page < pages - 1) {
            inv.setItem(NEXT_SLOT, GuideBookGui.button(Material.ARROW,
                "Next ►", NamedTextColor.WHITE, "Page " + (page + 2)));
        }
    }

    private static String ingName(Object ing) {
        if (ing instanceof FDItems fd) return fd.getDisplayName();
        if (ing instanceof Material mat) return matName(mat);
        return "Unknown";
    }

    private static String matName(Material mat) {
        String s = mat.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String stackName(ItemStack stack) {
        if (stack == null) return "Unknown";
        FDItems fd = FDItems.fromStack(stack);
        if (fd != null) return fd.getDisplayName();
        return matName(stack.getType());
    }

    private static ItemStack ingToStack(Object ing) {
        if (ing instanceof FDItems fd) return fd.create();
        if (ing instanceof Material mat) return new ItemStack(mat);
        return new ItemStack(Material.BARRIER);
    }
}
