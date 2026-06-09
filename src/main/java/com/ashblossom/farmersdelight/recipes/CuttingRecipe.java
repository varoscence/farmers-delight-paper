package com.ashblossom.farmersdelight.recipes;

import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class CuttingRecipe {
    public record ChanceOutput(ItemStack item, float chance) {}

    private final Object input; // FDItems or Material
    private final List<ChanceOutput> outputs;

    private CuttingRecipe(Object input, List<ChanceOutput> outputs) {
        this.input = input;
        this.outputs = outputs;
    }

    public boolean matches(ItemStack stack) {
        if (stack == null) return false;
        if (input instanceof FDItems fd) return fd == FDItems.fromStack(stack);
        if (input instanceof Material mat) return stack.getType() == mat;
        return false;
    }

    public List<ChanceOutput> getOutputs() { return outputs; }

    public static Builder of(FDItems item) { return new Builder(item); }
    public static Builder of(Material material) { return new Builder(material); }

    public static class Builder {
        private final Object input;
        private final List<ChanceOutput> outputs = new ArrayList<>();
        Builder(Object input) { this.input = input; }
        public Builder gives(ItemStack item) { outputs.add(new ChanceOutput(item, 1.0f)); return this; }
        public Builder gives(ItemStack item, float chance) { outputs.add(new ChanceOutput(item, chance)); return this; }
        public CuttingRecipe build() { return new CuttingRecipe(input, List.copyOf(outputs)); }
    }
}
