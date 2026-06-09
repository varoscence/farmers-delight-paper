package com.ashblossom.farmersdelight.recipes;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;

import java.util.ArrayList;
import java.util.List;

public class FDRecipes {

    public static final List<CuttingRecipe> CUTTING = new ArrayList<>();
    public static final List<CookingRecipe> COOKING = new ArrayList<>();

    public static void register(FarmersDelightPlugin plugin) {
        registerCutting();
        registerCooking();
        registerCrafting(plugin);
        registerSmelting(plugin);
    }

    // ─── Cutting Board ───────────────────────────────────────────────────────

    private static void registerCutting() {
        // Knives
        CUTTING.add(CuttingRecipe.of(Material.FLINT).gives(FDItems.FLINT_KNIFE.create()).build());

        // Meat processing
        CUTTING.add(CuttingRecipe.of(Material.BEEF).gives(new org.bukkit.inventory.ItemStack(Material.BEEF)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.PORKCHOP).gives(new org.bukkit.inventory.ItemStack(Material.PORKCHOP)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.CHICKEN).gives(new org.bukkit.inventory.ItemStack(Material.CHICKEN)).gives(new org.bukkit.inventory.ItemStack(Material.FEATHER)).build());
        CUTTING.add(CuttingRecipe.of(Material.RABBIT).gives(new org.bukkit.inventory.ItemStack(Material.RABBIT)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.MUTTON).gives(new org.bukkit.inventory.ItemStack(Material.MUTTON)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.COD).gives(new org.bukkit.inventory.ItemStack(Material.COD)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.SALMON).gives(new org.bukkit.inventory.ItemStack(Material.SALMON)).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());

        // FD meat
        CUTTING.add(CuttingRecipe.of(FDItems.MINCED_BEEF).gives(FDItems.MINCED_BEEF.create()).gives(FDItems.BONE_MEAL_BAG.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(FDItems.SMOKED_HAM).gives(FDItems.HAM_SLICE.create(2)).build());
        CUTTING.add(CuttingRecipe.of(FDItems.BACON).gives(FDItems.BACON.create(2)).build());

        // Vegetables
        CUTTING.add(CuttingRecipe.of(FDItems.CABBAGE).gives(FDItems.CABBAGE_LEAF.create(4)).build());
        CUTTING.add(CuttingRecipe.of(FDItems.TOMATO).gives(FDItems.TOMATO_SLICE.create(2)).build());
        CUTTING.add(CuttingRecipe.of(FDItems.ONION).gives(FDItems.ONION.create()).gives(FDItems.ONION.create(), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.CARROT).gives(new org.bukkit.inventory.ItemStack(Material.CARROT)).gives(new org.bukkit.inventory.ItemStack(Material.CARROT, 1), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.POTATO).gives(new org.bukkit.inventory.ItemStack(Material.POTATO)).gives(new org.bukkit.inventory.ItemStack(Material.POTATO, 1), 0.5f).build());
        CUTTING.add(CuttingRecipe.of(Material.PUMPKIN).gives(new org.bukkit.inventory.ItemStack(Material.PUMPKIN_SEEDS, 4)).gives(FDItems.PUMPKIN_SLICE.create(4)).build());
        CUTTING.add(CuttingRecipe.of(Material.MELON).gives(new org.bukkit.inventory.ItemStack(Material.MELON_SLICE, 9)).build());
        CUTTING.add(CuttingRecipe.of(Material.KELP).gives(new org.bukkit.inventory.ItemStack(Material.DRIED_KELP_BLOCK)).build());

        // Bread
        CUTTING.add(CuttingRecipe.of(Material.BREAD).gives(FDItems.BREAD_SLICE.create(2)).build());
        CUTTING.add(CuttingRecipe.of(FDItems.CAKE_SLICE).gives(FDItems.CAKE_SLICE.create()).build());

        // Tree bark / wood logs
        CUTTING.add(CuttingRecipe.of(Material.OAK_LOG).gives(new org.bukkit.inventory.ItemStack(Material.OAK_PLANKS, 6)).build());
        CUTTING.add(CuttingRecipe.of(Material.SPRUCE_LOG).gives(new org.bukkit.inventory.ItemStack(Material.SPRUCE_PLANKS, 6)).build());
        CUTTING.add(CuttingRecipe.of(Material.BIRCH_LOG).gives(new org.bukkit.inventory.ItemStack(Material.BIRCH_PLANKS, 6)).build());
        CUTTING.add(CuttingRecipe.of(Material.JUNGLE_LOG).gives(new org.bukkit.inventory.ItemStack(Material.JUNGLE_PLANKS, 6)).build());

        // Kelp roll
        CUTTING.add(CuttingRecipe.of(FDItems.KELP_ROLL).gives(FDItems.KELP_ROLL_SLICE.create(4)).build());
    }

    // ─── Cooking Pot ─────────────────────────────────────────────────────────

    private static void registerCooking() {
        // Stews & soups
        COOKING.add(CookingRecipe.makes(FDItems.VEGETABLE_SOUP.create())
            .needs(FDItems.CABBAGE).needs(FDItems.TOMATO).needs(Material.CARROT).needs(Material.POTATO)
            .returns(Material.BOWL).cookTime(1200).build());
        COOKING.add(CookingRecipe.makes(FDItems.BEEF_STEW.create())
            .needs(Material.BEEF).needs(Material.CARROT).needs(Material.POTATO).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1600).build());
        COOKING.add(CookingRecipe.makes(FDItems.CHICKEN_SOUP.create())
            .needs(Material.CHICKEN).needs(FDItems.CABBAGE_LEAF).needs(Material.CARROT).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1200).build());
        COOKING.add(CookingRecipe.makes(FDItems.FISH_STEW.create())
            .needs(Material.COD).needs(Material.POTATO).needs(FDItems.TOMATO).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1200).build());
        COOKING.add(CookingRecipe.makes(FDItems.PUMPKIN_SOUP.create())
            .needs(FDItems.PUMPKIN_SLICE).needs(Material.CARROT).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1000).build());
        COOKING.add(CookingRecipe.makes(new org.bukkit.inventory.ItemStack(Material.MUSHROOM_STEW))
            .needs(Material.BROWN_MUSHROOM).needs(Material.RED_MUSHROOM).needs(Material.POTATO)
            .returns(Material.BOWL).cookTime(1000).build());
        COOKING.add(CookingRecipe.makes(FDItems.NOODLE_SOUP.create())
            .needs(FDItems.WHEAT_DOUGH).needs(Material.CARROT).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1200).build());
        COOKING.add(CookingRecipe.makes(FDItems.RAMEN.create())
            .needs(FDItems.WHEAT_DOUGH).needs(Material.BEEF).needs(FDItems.CABBAGE_LEAF).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1600).build());
        COOKING.add(CookingRecipe.makes(FDItems.BORSCHT.create())
            .needs(Material.BEETROOT).needs(Material.POTATO).needs(FDItems.CABBAGE_LEAF)
            .returns(Material.BOWL).cookTime(1200).build());
        COOKING.add(CookingRecipe.makes(FDItems.POTATO_SOUP.create())
            .needs(Material.POTATO).needs(FDItems.ONION).needs(Material.MILK_BUCKET)
            .returns(Material.BOWL).cookTime(1000).build());

        // Meals on plates
        COOKING.add(CookingRecipe.makes(FDItems.COOKED_RICE.create())
            .needs(FDItems.RICE).needs(FDItems.ONION).needs(Material.CARROT)
            .returns(Material.BOWL).cookTime(800).build());
        COOKING.add(CookingRecipe.makes(FDItems.FRIED_RICE.create())
            .needs(FDItems.RICE).needs(Material.EGG).needs(FDItems.ONION).needs(Material.CARROT)
            .returns(Material.BOWL).cookTime(1000).build());
        COOKING.add(CookingRecipe.makes(FDItems.PASTA_WITH_MEATBALLS.create())
            .needs(FDItems.WHEAT_DOUGH).needs(FDItems.MINCED_BEEF).needs(FDItems.TOMATO)
            .returns(Material.BOWL).cookTime(1600).build());
        COOKING.add(CookingRecipe.makes(FDItems.PASTA_WITH_MUTTON_CHOP.create())
            .needs(FDItems.WHEAT_DOUGH).needs(Material.MUTTON).needs(FDItems.ONION)
            .returns(Material.BOWL).cookTime(1600).build());

        // Sandwiches & burgers (assembled after basic cooking)
        COOKING.add(CookingRecipe.makes(FDItems.HAMBURGER.create())
            .needs(FDItems.BREAD_SLICE).needs(FDItems.MINCED_BEEF).needs(FDItems.TOMATO_SLICE).needs(FDItems.CABBAGE_LEAF)
            .returns(Material.AIR).cookTime(600).build());
        COOKING.add(CookingRecipe.makes(FDItems.CHICKEN_SANDWICH.create())
            .needs(FDItems.BREAD_SLICE).needs(Material.COOKED_CHICKEN).needs(FDItems.TOMATO_SLICE).needs(FDItems.CABBAGE_LEAF)
            .returns(Material.AIR).cookTime(600).build());
        COOKING.add(CookingRecipe.makes(FDItems.BACON_SANDWICH.create())
            .needs(FDItems.BREAD_SLICE).needs(FDItems.BACON).needs(FDItems.TOMATO_SLICE)
            .returns(Material.AIR).cookTime(600).build());
        COOKING.add(CookingRecipe.makes(FDItems.MUTTON_WRAP.create())
            .needs(FDItems.WHEAT_DOUGH).needs(Material.COOKED_MUTTON).needs(FDItems.TOMATO_SLICE).needs(FDItems.CABBAGE_LEAF)
            .returns(Material.AIR).cookTime(800).build());
        COOKING.add(CookingRecipe.makes(FDItems.SALMON_ROLL.create())
            .needs(FDItems.WHEAT_DOUGH).needs(Material.COOKED_SALMON).needs(FDItems.TOMATO_SLICE)
            .returns(Material.AIR).cookTime(800).build());

        // Beverages
        COOKING.add(CookingRecipe.makes(FDItems.HOT_COCOA.create())
            .needs(Material.COCOA_BEANS).needs(Material.MILK_BUCKET).needs(Material.SUGAR)
            .returns(Material.GLASS_BOTTLE).cookTime(800).build());
        COOKING.add(CookingRecipe.makes(FDItems.APPLE_CIDER.create())
            .needs(Material.APPLE).needs(Material.APPLE).needs(Material.SUGAR)
            .returns(Material.GLASS_BOTTLE).cookTime(1000).build());
        COOKING.add(CookingRecipe.makes(FDItems.MELON_JUICE.create())
            .needs(Material.MELON_SLICE).needs(Material.MELON_SLICE).needs(Material.SUGAR)
            .returns(Material.GLASS_BOTTLE).cookTime(600).build());
        COOKING.add(CookingRecipe.makes(FDItems.TOMATO_SAUCE.create())
            .needs(FDItems.TOMATO).needs(FDItems.TOMATO).needs(FDItems.ONION)
            .returns(Material.GLASS_BOTTLE).cookTime(800).build());
        COOKING.add(CookingRecipe.makes(FDItems.HONEY_GLAZED_HAM.create())
            .needs(FDItems.SMOKED_HAM).needs(Material.HONEY_BOTTLE).needs(FDItems.ONION)
            .returns(Material.AIR).cookTime(2000).build());
        COOKING.add(CookingRecipe.makes(FDItems.ROAST_CHICKEN_BLOCK.create())
            .needs(Material.CHICKEN).needs(FDItems.ONION).needs(FDItems.TOMATO).needs(Material.CARROT)
            .returns(Material.AIR).cookTime(2000).build());

        // Feasts / large dishes
        COOKING.add(CookingRecipe.makes(FDItems.SHEPHERDS_PIE.create())
            .needs(Material.COOKED_MUTTON).needs(Material.POTATO).needs(FDItems.ONION).needs(Material.CARROT)
            .returns(Material.AIR).cookTime(2400).build());
        COOKING.add(CookingRecipe.makes(FDItems.APPLE_PIE.create())
            .needs(Material.APPLE).needs(Material.APPLE).needs(Material.SUGAR).needs(FDItems.WHEAT_DOUGH)
            .returns(Material.AIR).cookTime(1600).build());
        COOKING.add(CookingRecipe.makes(FDItems.BIRTHDAY_CAKE.create())
            .needs(Material.SUGAR).needs(Material.EGG).needs(Material.MILK_BUCKET).needs(FDItems.WHEAT_DOUGH)
            .returns(Material.AIR).cookTime(2000).build());
        COOKING.add(CookingRecipe.makes(FDItems.KELP_ROLL.create())
            .needs(FDItems.RICE).needs(Material.KELP).needs(FDItems.TOMATO_SLICE)
            .returns(Material.AIR).cookTime(800).build());
    }

    // ─── Crafting ────────────────────────────────────────────────────────────

    private static void registerCrafting(FarmersDelightPlugin plugin) {
        // Knives
        shapeless(plugin, "flint_knife", FDItems.FLINT_KNIFE.create(), Material.FLINT, Material.STICK);
        shapeless(plugin, "iron_knife", FDItems.IRON_KNIFE.create(), Material.IRON_INGOT, Material.STICK);
        shapeless(plugin, "golden_knife", FDItems.GOLDEN_KNIFE.create(), Material.GOLD_INGOT, Material.STICK);
        shapeless(plugin, "diamond_knife", FDItems.DIAMOND_KNIFE.create(), Material.DIAMOND, Material.STICK);
        shapeless(plugin, "netherite_knife", FDItems.NETHERITE_KNIFE.create(), FDItems.DIAMOND_KNIFE.create(), Material.NETHERITE_INGOT);

        // Cooking Pot & Cutting Board
        shaped(plugin, "cooking_pot", FDItems.COOKING_POT_ITEM.create(),
            "III", "I I", "FFF",
            'I', Material.IRON_INGOT, 'F', Material.FURNACE);
        shaped(plugin, "cutting_board", FDItems.CUTTING_BOARD_ITEM.create(),
            "PPP", "SSS",
            'P', Material.OAK_PLANKS, 'S', Material.OAK_SLAB);

        // Straw & dough
        shapeless(plugin, "wheat_dough", FDItems.WHEAT_DOUGH.create(), Material.WHEAT, Material.WHEAT, Material.WATER_BUCKET);
        shapeless(plugin, "rice_panicle_to_rice", FDItems.RICE.create(4), FDItems.RICE_PANICLE.create());

        // Organic compounds
        shapeless(plugin, "bone_meal_bag", FDItems.BONE_MEAL_BAG.create(3), Material.BONE_MEAL, Material.BONE_MEAL, Material.BONE_MEAL);
        shaped(plugin, "straw_block", FDItems.STRAW_BALE.create(),
            "SSS", "SSS", "SSS", 'S', Material.WHEAT);

        // Animal products
        shapeless(plugin, "canvas", FDItems.CANVAS.create(), Material.STRING, Material.STRING, Material.STRING, Material.STRING);
        shapeless(plugin, "hemp_rope", FDItems.HEMP_ROPE.create(4), FDItems.CANVAS.create());
        shapeless(plugin, "rope_roll", FDItems.ROPE_ROLL.create(), FDItems.HEMP_ROPE.create(), FDItems.HEMP_ROPE.create(), FDItems.HEMP_ROPE.create());


        // Sack
        shaped(plugin, "sack", FDItems.SACK.create(),
            "CCC", "CBC", "CCC",
            'C', FDItems.CANVAS.create(), 'B', Material.BROWN_DYE);
        shaped(plugin, "basket", FDItems.BASKET.create(),
            "SBS", "BSB", "SBS",
            'S', Material.BAMBOO, 'B', Material.BAMBOO_SLAB);

        // Desserts - assembled without cooking
        shapeless(plugin, "cake_slice", FDItems.CAKE_SLICE.create(), Material.CAKE);
    }

    // ─── Smelting / Smoking ──────────────────────────────────────────────────

    private static void registerSmelting(FarmersDelightPlugin plugin) {
        smelt(plugin, "smoked_ham", FDItems.SMOKED_HAM.create(), Material.PORKCHOP, 0.35f, 200);
        smelt(plugin, "minced_beef_cooked", FDItems.MINCED_BEEF.create(), Material.BEEF, 0.35f, 200);
        smelt(plugin, "cooked_bacon", FDItems.BACON.create(), FDItems.BACON.create(), 0.35f, 200);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static void shaped(FarmersDelightPlugin plugin, String id, ItemStack result, Object... args) {
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, id), result);
        String[] shape = null;
        int i = 0;
        List<String> shapeLines = new ArrayList<>();
        while (i < args.length && args[i] instanceof String s) { shapeLines.add(s); i++; }
        recipe.shape(shapeLines.toArray(new String[0]));
        while (i < args.length) {
            char c = (char) args[i++];
            Object ing = args[i++];
            if (ing instanceof ItemStack is) recipe.setIngredient(c, new RecipeChoice.ExactChoice(is));
            else if (ing instanceof Material mat) recipe.setIngredient(c, mat);
        }
        plugin.getServer().addRecipe(recipe);
    }

    private static void shapeless(FarmersDelightPlugin plugin, String id, ItemStack result, Object... ingredients) {
        ShapelessRecipe recipe = new ShapelessRecipe(new NamespacedKey(plugin, id), result);
        for (Object ing : ingredients) {
            if (ing instanceof ItemStack is) recipe.addIngredient(new RecipeChoice.ExactChoice(is));
            else if (ing instanceof Material mat) recipe.addIngredient(mat);
        }
        plugin.getServer().addRecipe(recipe);
    }

    private static void smelt(FarmersDelightPlugin plugin, String id, ItemStack result, Object input, float xp, int time) {
        RecipeChoice choice = (input instanceof ItemStack is)
            ? new RecipeChoice.ExactChoice(is)
            : new RecipeChoice.MaterialChoice((Material) input);
        FurnaceRecipe furnace = new FurnaceRecipe(new NamespacedKey(plugin, id+"_furnace"), result, choice, xp, time);
        SmokingRecipe smoking = new SmokingRecipe(new NamespacedKey(plugin, id+"_smoking"), result, choice, xp, time/2);
        plugin.getServer().addRecipe(furnace);
        plugin.getServer().addRecipe(smoking);
    }
}
