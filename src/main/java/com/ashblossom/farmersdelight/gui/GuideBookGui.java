package com.ashblossom.farmersdelight.gui;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GuideBookGui {

    private static final NamespacedKey GUIDE_KEY =
        new NamespacedKey(FarmersDelightPlugin.get(), "guide");

    public static ItemStack createGuideItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Farmer's Guide")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        meta.lore(List.of(
            Component.text("A guide to the art of cooking.").color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false),
            Component.text("Right-click to open.").color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(GUIDE_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isGuideBook(ItemStack stack) {
        if (stack == null || stack.getType() != Material.BOOK) return false;
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer()
            .has(GUIDE_KEY, PersistentDataType.BYTE);
    }

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(
            new FDInventoryHolder(FDInventoryHolder.GuiType.GUIDE_MAIN, 0),
            27,
            Component.text("Farmer's Guide").color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
        );

        ItemStack pane = glass(Material.LIME_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inv.setItem(i, pane);

        inv.setItem(11, button(Material.CAULDRON,
            "Cooking Pot Recipes", NamedTextColor.GREEN,
            "Browse all cooking pot recipes."));
        inv.setItem(13, button(Material.OAK_SLAB,
            "Cutting Board Recipes", NamedTextColor.YELLOW,
            "Browse all cutting board recipes."));
        inv.setItem(15, button(Material.CRAFTING_TABLE,
            "Crafting Recipes", NamedTextColor.AQUA,
            "Open your crafting table and click",
            "the recipe book icon (bottom-left)."));

        player.openInventory(inv);
    }

    static ItemStack glass(Material mat) {
        ItemStack i = new ItemStack(mat);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.empty());
        i.setItemMeta(m);
        return i;
    }

    static ItemStack button(Material mat, String name, NamedTextColor color, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name).color(color)
            .decoration(TextDecoration.ITALIC, false));
        meta.lore(Arrays.stream(lore)
            .map(l -> Component.text(l).color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false))
            .collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }
}
