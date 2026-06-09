package com.ashblossom.farmersdelight.items;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public enum FDItems {

    // TOOLS
    FLINT_KNIFE(    "flint_knife",    "Flint Knife",     Material.WOODEN_SWORD,   1001, 0, 0, null, ItemType.KNIFE, 2.0f, 1.6f),
    IRON_KNIFE(     "iron_knife",     "Iron Knife",      Material.IRON_SWORD,     1002, 0, 0, null, ItemType.KNIFE, 3.0f, 1.8f),
    GOLDEN_KNIFE(   "golden_knife",   "Golden Knife",    Material.GOLDEN_SWORD,   1003, 0, 0, null, ItemType.KNIFE, 2.0f, 2.0f),
    DIAMOND_KNIFE(  "diamond_knife",  "Diamond Knife",   Material.DIAMOND_SWORD,  1004, 0, 0, null, ItemType.KNIFE, 4.0f, 1.8f),
    NETHERITE_KNIFE("netherite_knife","Netherite Knife", Material.NETHERITE_SWORD,1005, 0, 0, null, ItemType.KNIFE, 4.5f, 1.9f),

    // MATERIALS
    STRAW(        "straw",        "Straw",        Material.PAPER, 2001, 0, 0, null, ItemType.MATERIAL, 0, 0),
    CANVAS(       "canvas",       "Canvas",       Material.PAPER, 2002, 0, 0, null, ItemType.MATERIAL, 0, 0),
    TREE_BARK(    "tree_bark",    "Tree Bark",    Material.PAPER, 2003, 0, 0, null, ItemType.MATERIAL, 0, 0),
    HEMP_ROPE(    "rope",         "Hemp Rope",    Material.PAPER, 2004, 0, 0, null, ItemType.MATERIAL, 0, 0),
    ROPE_ROLL(    "rope_roll",    "Rope Roll",    Material.PAPER, 2009, 0, 0, null, ItemType.MATERIAL, 0, 0),
    WHEAT_DOUGH(  "wheat_dough",  "Wheat Dough",  Material.PAPER, 2005, 2, 0.3f, e(PotionEffectType.HUNGER, 100, 0), ItemType.FOOD, 0, 0),
    RAW_PASTA(    "raw_pasta",    "Raw Pasta",    Material.PAPER, 2006, 2, 0.3f, e(PotionEffectType.HUNGER, 100, 0), ItemType.FOOD, 0, 0),
    PIE_CRUST(    "pie_crust",    "Pie Crust",    Material.PAPER, 2007, 2, 0.2f, null, ItemType.FOOD, 0, 0),
    ROTTEN_TOMATO("rotten_tomato","Rotten Tomato",Material.PAPER, 2008, 2, 0.1f, e(PotionEffectType.HUNGER, 200, 0), ItemType.FOOD, 0, 0),

    // CROPS
    CABBAGE(      "cabbage",       "Cabbage",       Material.PAPER, 3001, 2, 0.4f, null, ItemType.FOOD, 0, 0),
    CABBAGE_SEEDS("cabbage_seeds", "Cabbage Seeds", Material.PAPER, 3002, 0, 0,    null, ItemType.MATERIAL, 0, 0),
    TOMATO(       "tomato",        "Tomato",        Material.PAPER, 3003, 1, 0.3f, null, ItemType.FOOD, 0, 0),
    TOMATO_SEEDS( "tomato_seeds",  "Tomato Seeds",  Material.PAPER, 3004, 0, 0,    null, ItemType.MATERIAL, 0, 0),
    ONION(        "onion",         "Onion",         Material.PAPER, 3005, 2, 0.4f, null, ItemType.FOOD, 0, 0),
    RICE(         "rice",          "Rice",          Material.PAPER, 3006, 0, 0,    null, ItemType.MATERIAL, 0, 0),
    RICE_PANICLE( "rice_panicle",  "Rice Panicle",  Material.PAPER, 3007, 0, 0,    null, ItemType.MATERIAL, 0, 0),

    // RAW INGREDIENTS
    FRIED_EGG(          "fried_egg",           "Fried Egg",           Material.PAPER, 4001, 4, 0.4f, null, ItemType.FOOD, 0, 0),
    PUMPKIN_SLICE(      "pumpkin_slice",        "Pumpkin Slice",       Material.PAPER, 4002, 3, 0.3f, null, ItemType.FOOD, 0, 0),
    CABBAGE_LEAF(       "cabbage_leaf",         "Cabbage Leaf",        Material.PAPER, 4003, 1, 0.4f, null, ItemType.FOOD, 0, 0),
    MINCED_BEEF(        "minced_beef",          "Minced Beef",         Material.PAPER, 4004, 2, 0.3f, null, ItemType.FOOD, 0, 0),
    BEEF_PATTY(         "beef_patty",           "Beef Patty",          Material.PAPER, 4005, 4, 0.8f, null, ItemType.FOOD, 0, 0),
    CHICKEN_CUTS(       "chicken_cuts",         "Chicken Cuts",        Material.PAPER, 4006, 1, 0.3f, e(PotionEffectType.HUNGER, 200, 0), ItemType.FOOD, 0, 0),
    COOKED_CHICKEN_CUTS("cooked_chicken_cuts",  "Cooked Chicken Cuts", Material.PAPER, 4007, 3, 0.6f, null, ItemType.FOOD, 0, 0),
    BACON(              "bacon",                "Bacon",               Material.PAPER, 4008, 2, 0.3f, null, ItemType.FOOD, 0, 0),
    COOKED_BACON(       "cooked_bacon",         "Cooked Bacon",        Material.PAPER, 4009, 4, 0.8f, null, ItemType.FOOD, 0, 0),
    COD_SLICE(          "cod_slice",            "Cod Slice",           Material.PAPER, 4010, 1, 0.1f, null, ItemType.FOOD, 0, 0),
    COOKED_COD_SLICE(   "cooked_cod_slice",     "Cooked Cod Slice",    Material.PAPER, 4011, 3, 0.5f, null, ItemType.FOOD, 0, 0),
    SALMON_SLICE(       "salmon_slice",         "Salmon Slice",        Material.PAPER, 4012, 1, 0.1f, null, ItemType.FOOD, 0, 0),
    COOKED_SALMON_SLICE("cooked_salmon_slice",  "Cooked Salmon Slice", Material.PAPER, 4013, 3, 0.8f, null, ItemType.FOOD, 0, 0),
    MUTTON_CHOPS(       "mutton_chops",         "Mutton Chops",        Material.PAPER, 4014, 1, 0.3f, null, ItemType.FOOD, 0, 0),
    COOKED_MUTTON_CHOPS("cooked_mutton_chops",  "Cooked Mutton Chops", Material.PAPER, 4015, 3, 0.8f, null, ItemType.FOOD, 0, 0),
    HAM(                "ham",                  "Ham",                 Material.PAPER, 4016, 5, 0.3f, null, ItemType.FOOD, 0, 0),
    SMOKED_HAM(         "smoked_ham",           "Smoked Ham",          Material.PAPER, 4017, 10, 0.8f, null, ItemType.FOOD, 0, 0),
    BONE_MEAL_BAG(      "bone_meal_bag",        "Bone Meal Bag",       Material.PAPER, 4018, 0, 0, null, ItemType.MATERIAL, 0, 0),
    HAM_SLICE(          "ham_slice",            "Ham Slice",           Material.PAPER, 4019, 4, 0.6f, null, ItemType.FOOD, 0, 0),
    TOMATO_SLICE(       "tomato_slice",         "Tomato Slice",        Material.PAPER, 4020, 1, 0.2f, null, ItemType.FOOD, 0, 0),
    BREAD_SLICE(        "bread_slice",          "Bread Slice",         Material.PAPER, 4021, 3, 0.3f, null, ItemType.FOOD, 0, 0),

    // BEVERAGES
    MILK_BOTTLE( "milk_bottle",  "Milk Bottle",  Material.PAPER, 5001, 0, 0,    null, ItemType.DRINK, 0, 0),
    HOT_COCOA(   "hot_cocoa",    "Hot Cocoa",    Material.PAPER, 5002, 2, 0.2f, null, ItemType.DRINK, 0, 0),
    APPLE_CIDER( "apple_cider",  "Apple Cider",  Material.PAPER, 5003, 4, 0.4f, e(PotionEffectType.ABSORPTION, 600, 0), ItemType.DRINK, 0, 0),
    MELON_JUICE( "melon_juice",  "Melon Juice",  Material.PAPER, 5004, 4, 0.4f, null, ItemType.DRINK, 0, 0),
    TOMATO_SAUCE("tomato_sauce", "Tomato Sauce", Material.PAPER, 5005, 4, 0.4f, null, ItemType.FOOD, 0, 0),
    BONE_BROTH(  "bone_broth",   "Bone Broth",   Material.PAPER, 5006, 8, 0.7f, e(PotionEffectType.REGENERATION, 80, 0), ItemType.BOWL_FOOD, 0, 0),

    // DESSERTS
    CAKE_SLICE(                  "cake_slice",                "Cake Slice",                Material.PAPER, 6001, 2, 0.1f, e(PotionEffectType.SPEED, 100, 0), ItemType.FOOD, 0, 0),
    APPLE_PIE_SLICE(             "apple_pie_slice",           "Apple Pie Slice",           Material.PAPER, 6002, 3, 0.3f, e(PotionEffectType.SPEED, 100, 0), ItemType.FOOD, 0, 0),
    SWEET_BERRY_CHEESECAKE_SLICE("sweet_berry_cheesecake_slice","Sweet Berry Cheesecake Slice",Material.PAPER,6003,3,0.3f,e(PotionEffectType.SPEED,100,0),ItemType.FOOD,0,0),
    CHOCOLATE_PIE_SLICE(         "chocolate_pie_slice",       "Chocolate Pie Slice",       Material.PAPER, 6004, 3, 0.3f, e(PotionEffectType.SPEED, 100, 0), ItemType.FOOD, 0, 0),
    PUMPKIN_PIE_SLICE(           "pumpkin_pie_slice",         "Pumpkin Pie Slice",         Material.PAPER, 6005, 3, 0.3f, e(PotionEffectType.SPEED, 100, 0), ItemType.FOOD, 0, 0),
    SWEET_BERRY_COOKIE(          "sweet_berry_cookie",        "Sweet Berry Cookie",        Material.PAPER, 6006, 2, 0.1f, null, ItemType.FOOD, 0, 0),
    HONEY_COOKIE(                "honey_cookie",              "Honey Cookie",              Material.PAPER, 6007, 2, 0.1f, null, ItemType.FOOD, 0, 0),
    MELON_POPSICLE(              "melon_popsicle",            "Melon Popsicle",            Material.PAPER, 6008, 3, 0.2f, null, ItemType.FOOD, 0, 0),
    GLOW_BERRY_CUSTARD(          "glow_berry_custard",        "Glow Berry Custard",        Material.PAPER, 6009, 7, 0.6f, e(PotionEffectType.GLOWING, 200, 0), ItemType.FOOD, 0, 0),
    FRUIT_SALAD(                 "fruit_salad",               "Fruit Salad",               Material.PAPER, 6010, 6, 0.6f, e(PotionEffectType.REGENERATION, 60, 0), ItemType.FOOD, 0, 0),

    // MEALS
    MIXED_SALAD(    "mixed_salad",     "Mixed Salad",     Material.PAPER, 7001, 6,  0.6f, e(PotionEffectType.REGENERATION, 60, 0),  ItemType.FOOD, 0, 0),
    NETHER_SALAD(   "nether_salad",    "Nether Salad",    Material.PAPER, 7002, 5,  0.4f, e(PotionEffectType.NAUSEA, 200, 0),      ItemType.FOOD, 0, 0),
    BARBECUE_STICK( "barbecue_stick",  "Barbecue Stick",  Material.PAPER, 7003, 8,  0.9f, null, ItemType.FOOD, 0, 0),
    EGG_SANDWICH(   "egg_sandwich",    "Egg Sandwich",    Material.PAPER, 7004, 8,  0.8f, null, ItemType.FOOD, 0, 0),
    CHICKEN_SANDWICH("chicken_sandwich","Chicken Sandwich",Material.PAPER,7005, 10, 0.8f, null, ItemType.FOOD, 0, 0),
    HAMBURGER(      "hamburger",       "Hamburger",       Material.PAPER, 7006, 11, 0.8f, null, ItemType.FOOD, 0, 0),
    BACON_SANDWICH( "bacon_sandwich",  "Bacon Sandwich",  Material.PAPER, 7007, 10, 0.8f, null, ItemType.FOOD, 0, 0),
    MUTTON_WRAP(    "mutton_wrap",     "Mutton Wrap",     Material.PAPER, 7008, 10, 0.8f, null, ItemType.FOOD, 0, 0),
    DUMPLINGS(      "dumplings",       "Dumplings",       Material.PAPER, 7009, 8,  0.8f, null, ItemType.FOOD, 0, 0),
    STUFFED_POTATO( "stuffed_potato",  "Stuffed Potato",  Material.PAPER, 7010, 10, 0.7f, null, ItemType.FOOD, 0, 0),
    CABBAGE_ROLLS(  "cabbage_rolls",   "Cabbage Rolls",   Material.PAPER, 7011, 5,  0.5f, null, ItemType.FOOD, 0, 0),
    SALMON_ROLL(    "salmon_roll",     "Salmon Roll",     Material.PAPER, 7012, 7,  0.6f, null, ItemType.FOOD, 0, 0),
    COD_ROLL(       "cod_roll",        "Cod Roll",        Material.PAPER, 7013, 7,  0.6f, null, ItemType.FOOD, 0, 0),
    KELP_ROLL(      "kelp_roll",       "Kelp Roll",       Material.PAPER, 7014, 12, 2.4f, null, ItemType.FOOD, 0, 0),
    KELP_ROLL_SLICE("kelp_roll_slice", "Kelp Roll Slice", Material.PAPER, 7015, 6,  0.5f, null, ItemType.FOOD, 0, 0),

    // SOUPS & STEWS (bowl foods — return bowl on eat)
    COOKED_RICE(   "cooked_rice",    "Cooked Rice",    Material.PAPER, 8001, 6,  0.4f, e(PotionEffectType.SATURATION, 40, 0),    ItemType.BOWL_FOOD, 0, 0),
    BEEF_STEW(     "beef_stew",      "Beef Stew",      Material.PAPER, 8002, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    CHICKEN_SOUP(  "chicken_soup",   "Chicken Soup",   Material.PAPER, 8003, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    VEGETABLE_SOUP("vegetable_soup", "Vegetable Soup", Material.PAPER, 8004, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    FISH_STEW(     "fish_stew",      "Fish Stew",      Material.PAPER, 8005, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    FRIED_RICE(    "fried_rice",     "Fried Rice",     Material.PAPER, 8006, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    PUMPKIN_SOUP(  "pumpkin_soup",   "Pumpkin Soup",   Material.PAPER, 8007, 14, 0.75f,e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    BAKED_COD_STEW("baked_cod_stew", "Baked Cod Stew", Material.PAPER, 8008, 14, 0.75f,e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    NOODLE_SOUP(   "noodle_soup",    "Noodle Soup",    Material.PAPER, 8009, 14, 0.75f,e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    ONION_SOUP(    "onion_soup",     "Onion Soup",     Material.PAPER, 8010, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    RAMEN(         "ramen",          "Ramen",          Material.PAPER, 8011, 14, 0.75f,e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    BORSCHT(       "borscht",        "Borscht",        Material.PAPER, 8012, 12, 0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    POTATO_SOUP(   "potato_soup",    "Potato Soup",    Material.PAPER, 8013, 10, 0.6f, null, ItemType.BOWL_FOOD, 0, 0),
    RICE_BOWL(     "rice_bowl",      "Rice Bowl",      Material.PAPER, 8014, 8,  0.6f, null, ItemType.BOWL_FOOD, 0, 0),

    // PLATED MEALS (bowl foods)
    BACON_AND_EGGS(      "bacon_and_eggs",       "Bacon and Eggs",       Material.PAPER, 9001, 10, 0.6f,  e(PotionEffectType.REGENERATION, 80, 0),  ItemType.BOWL_FOOD, 0, 0),
    PASTA_WITH_MEATBALLS("pasta_with_meatballs", "Pasta with Meatballs", Material.PAPER, 9002, 12, 0.8f,  e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    PASTA_WITH_MUTTON_CHOP("pasta_with_mutton_chop","Pasta with Mutton Chop",Material.PAPER,9003,12,0.8f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    MUSHROOM_RICE(       "mushroom_rice",         "Mushroom Rice",        Material.PAPER, 9004, 12, 0.8f,  e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    ROASTED_MUTTON_CHOPS("roasted_mutton_chops",  "Roasted Mutton Chops", Material.PAPER, 9005, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    VEGETABLE_NOODLES(   "vegetable_noodles",     "Vegetable Noodles",    Material.PAPER, 9006, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    STEAK_AND_POTATOES(  "steak_and_potatoes",    "Steak and Potatoes",   Material.PAPER, 9007, 12, 0.8f,  e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),
    RATATOUILLE(         "ratatouille",           "Ratatouille",          Material.PAPER, 9008, 10, 0.6f,  e(PotionEffectType.REGENERATION, 80, 0),  ItemType.BOWL_FOOD, 0, 0),
    SQUID_INK_PASTA(     "squid_ink_pasta",       "Squid Ink Pasta",      Material.PAPER, 9009, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.BOWL_FOOD, 0, 0),
    GRILLED_SALMON(      "grilled_salmon",        "Grilled Salmon",       Material.PAPER, 9010, 14, 0.75f, e(PotionEffectType.REGENERATION, 100, 0), ItemType.BOWL_FOOD, 0, 0),

    // FEAST PORTIONS
    ROAST_CHICKEN(   "roast_chicken",    "Roast Chicken",    Material.PAPER, 10001, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.FOOD, 0, 0),
    STUFFED_PUMPKIN( "stuffed_pumpkin",  "Stuffed Pumpkin",  Material.PAPER, 10002, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.FOOD, 0, 0),
    HONEY_GLAZED_HAM("honey_glazed_ham", "Honey Glazed Ham", Material.PAPER, 10003, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.FOOD, 0, 0),
    SHEPHERDS_PIE(   "shepherds_pie",    "Shepherd's Pie",   Material.PAPER, 10004, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.FOOD, 0, 0),
    GLEAMING_SALAD(    "gleaming_salad",    "Gleaming Salad",    Material.PAPER, 10005, 14, 0.75f, e(PotionEffectType.REGENERATION, 160, 0), ItemType.FOOD, 0, 0),
    BIRTHDAY_CAKE(     "birthday_cake",    "Birthday Cake",     Material.PAPER, 10006, 6,  0.4f, e(PotionEffectType.ABSORPTION, 600, 1), ItemType.FOOD, 0, 0),
    APPLE_PIE(         "apple_pie",        "Apple Pie",         Material.PAPER, 10007, 14, 0.75f, null, ItemType.FOOD, 0, 0),
    ROAST_CHICKEN_BLOCK("roast_chicken_block","Roast Chicken Block",Material.PAPER,10008,14,0.75f,e(PotionEffectType.REGENERATION,200,0),ItemType.FOOD,0,0),

    // ANIMAL FOODS
    DOG_FOOD(  "dog_food",   "Dog Food",   Material.PAPER, 11001, 4, 0.2f, null, ItemType.FOOD, 0, 0),
    HORSE_FEED("horse_feed", "Horse Feed", Material.PAPER, 11002, 4, 0.2f, null, ItemType.FOOD, 0, 0),

    // FUNCTIONAL BLOCK ITEMS
    COOKING_POT_ITEM(  "cooking_pot",   "Cooking Pot",   Material.PAPER, 20001, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    CUTTING_BOARD_ITEM("cutting_board", "Cutting Board", Material.PAPER, 20002, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    STOVE(        "stove",         "Stove",         Material.PAPER, 20003, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    SKILLET(      "skillet",       "Skillet",       Material.PAPER, 20004, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    WOODEN_BASKET("wooden_basket", "Wooden Basket", Material.PAPER, 20005, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    BAMBOO_BASKET("bamboo_basket", "Bamboo Basket", Material.PAPER, 20006, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),

    // STORAGE & DECORATION
    CARROT_CRATE(   "carrot_crate",    "Carrot Crate",    Material.PAPER, 21001, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    POTATO_CRATE(   "potato_crate",    "Potato Crate",    Material.PAPER, 21002, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    BEETROOT_CRATE( "beetroot_crate",  "Beetroot Crate",  Material.PAPER, 21003, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    CABBAGE_CRATE(  "cabbage_crate",   "Cabbage Crate",   Material.PAPER, 21004, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    TOMATO_CRATE(   "tomato_crate",    "Tomato Crate",    Material.PAPER, 21005, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    ONION_CRATE(    "onion_crate",     "Onion Crate",     Material.PAPER, 21006, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    RICE_BALE(      "rice_bale",       "Rice Bale",       Material.PAPER, 21007, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    RICE_BAG(       "rice_bag",        "Rice Bag",        Material.PAPER, 21008, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    STRAW_BALE(     "straw_bale",      "Straw Bale",      Material.PAPER, 21009, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    ORGANIC_COMPOST("organic_compost", "Organic Compost", Material.PAPER, 21010, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    RICH_SOIL(      "rich_soil",       "Rich Soil",       Material.PAPER, 21011, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    SAFETY_NET(     "safety_net",      "Safety Net",      Material.PAPER, 21012, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    CANVAS_RUG(     "canvas_rug",      "Canvas Rug",      Material.PAPER, 21013, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    TATAMI(         "tatami",          "Tatami",          Material.PAPER, 21014, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    ROPE_FENCE(     "rope_fence",      "Rope Fence",      Material.PAPER, 21015, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    SACK(           "sack",            "Sack",            Material.PAPER, 21016, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0),
    BASKET(         "basket",          "Basket",          Material.PAPER, 21017, 0, 0, null, ItemType.BLOCK_ITEM, 0, 0);

    // ─────────────────────────────────────────────────────────────────────────

    public enum ItemType { FOOD, BOWL_FOOD, DRINK, KNIFE, MATERIAL, BLOCK_ITEM }

    private final String id;
    private final String displayName;
    private final Material baseMaterial;
    private final int cmd;
    private final int nutrition;
    private final float saturation;
    private final PotionEffect effect;
    private final ItemType type;
    private final float attackDamage;
    private final float attackSpeed;

    FDItems(String id, String displayName, Material baseMaterial, int cmd,
            int nutrition, float saturation, PotionEffect effect,
            ItemType type, float attackDamage, float attackSpeed) {
        this.id = id; this.displayName = displayName; this.baseMaterial = baseMaterial;
        this.cmd = cmd; this.nutrition = nutrition; this.saturation = saturation;
        this.effect = effect; this.type = type;
        this.attackDamage = attackDamage; this.attackSpeed = attackSpeed;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public int getCmd() { return cmd; }
    public ItemType getItemType() { return type; }
    public Material getBaseMaterial() { return baseMaterial; }
    public PotionEffect getEffect() { return effect; }

    public ItemStack create() { return createItem(1); }
    public ItemStack create(int amount) { return createItem(amount); }
    public ItemStack createItem() { return createItem(1); }

    public ItemStack createItem(int amount) {
        ItemStack item = new ItemStack(baseMaterial, amount);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(displayName)
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false));
        meta.setCustomModelData(cmd);

        if (nutrition > 0 || type == ItemType.BOWL_FOOD || type == ItemType.DRINK) {
            FoodComponent food = meta.getFood();
            if (food == null) {
                ItemStack donor = new ItemStack(Material.COOKIE);
                food = donor.getItemMeta().getFood();
            }
            food.setNutrition(nutrition);
            food.setSaturation(saturation);
            food.setCanAlwaysEat(false);
            // effects applied via FoodConsumeListener to avoid FoodComponent API version issues
            meta.setFood(food);
        }

        if (type == ItemType.KNIFE) {
            meta.addAttributeModifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(new NamespacedKey("farmersdelight", id + "_dmg"),
                    attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
            meta.addAttributeModifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(new NamespacedKey("farmersdelight", id + "_spd"),
                    attackSpeed - 4.0f, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
        }

        meta.getPersistentDataContainer().set(
            new NamespacedKey("farmersdelight", "item_id"),
            org.bukkit.persistence.PersistentDataType.STRING, id);

        item.setItemMeta(meta);
        return item;
    }

    public static FDItems byId(String id) {
        for (FDItems i : values()) if (i.id.equals(id)) return i;
        return null;
    }

    public static FDItems fromStack(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        String id = stack.getItemMeta().getPersistentDataContainer()
            .get(new NamespacedKey("farmersdelight", "item_id"),
                org.bukkit.persistence.PersistentDataType.STRING);
        return id != null ? byId(id) : null;
    }

    public static boolean isKnife(ItemStack stack) {
        FDItems i = fromStack(stack);
        return i != null && i.type == ItemType.KNIFE;
    }

    private static PotionEffect e(PotionEffectType type, int ticks, int amp) {
        return new PotionEffect(type, ticks, amp, false, false, false);
    }
}
