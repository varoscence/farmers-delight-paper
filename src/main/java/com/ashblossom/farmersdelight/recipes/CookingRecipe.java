package com.ashblossom.farmersdelight.recipes;

import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CookingRecipe {
    private final List<Object> ingredients; // FDItems or Material
    private final ItemStack output;
    private final Material container; // returned when output is taken
    private final int cookTime;

    private CookingRecipe(List<Object> ingredients, ItemStack output, Material container, int cookTime) {
        this.ingredients = ingredients;
        this.output = output;
        this.container = container;
        this.cookTime = cookTime;
    }

    public boolean matches(List<ItemStack> items) {
        List<Object> needed = new ArrayList<>(ingredients);
        for (ItemStack stack : items) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            boolean found = false;
            for (int i = 0; i < needed.size(); i++) {
                Object req = needed.get(i);
                if (matchesIngredient(req, stack)) { needed.remove(i); found = true; break; }
            }
            if (!found) return false;
        }
        return needed.isEmpty();
    }

    private boolean matchesIngredient(Object req, ItemStack stack) {
        if (req instanceof FDItems fd) return fd == FDItems.fromStack(stack);
        if (req instanceof Material mat) return stack.getType() == mat;
        return false;
    }

    public List<Object> getIngredients() { return ingredients; }
    public ItemStack getOutput() { return output.clone(); }
    public Material getContainer() { return container; }
    public int getCookTime() { return cookTime; }

    public static Builder makes(ItemStack output) { return new Builder(output); }

    public static class Builder {
        private final ItemStack output;
        private final List<Object> ingredients = new ArrayList<>();
        private Material container = Material.AIR;
        private int cookTime = 1200;
        Builder(ItemStack output) { this.output = output; }
        public Builder needs(FDItems item) { ingredients.add(item); return this; }
        public Builder needs(Material mat) { ingredients.add(mat); return this; }
        public Builder returns(Material mat) { container = mat; return this; }
        public Builder cookTime(int ticks) { cookTime = ticks; return this; }
        public CookingRecipe build() { return new CookingRecipe(List.copyOf(ingredients), output, container, cookTime); }
    }
}
