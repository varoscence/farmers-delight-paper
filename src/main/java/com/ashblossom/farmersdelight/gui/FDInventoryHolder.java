package com.ashblossom.farmersdelight.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class FDInventoryHolder implements InventoryHolder {
    public enum GuiType { GUIDE_MAIN, COOKING_RECIPES, CUTTING_RECIPES }

    public final GuiType type;
    public final int page;

    public FDInventoryHolder(GuiType type, int page) {
        this.type = type;
        this.page = page;
    }

    @Override
    public Inventory getInventory() { return null; }
}
