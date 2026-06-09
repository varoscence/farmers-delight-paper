package com.ashblossom.farmersdelight;

import com.ashblossom.farmersdelight.blocks.CookingPotManager;
import com.ashblossom.farmersdelight.blocks.CuttingBoardManager;
import com.ashblossom.farmersdelight.blocks.SkilletManager;
import com.ashblossom.farmersdelight.crops.CropManager;
import com.ashblossom.farmersdelight.listeners.AdminCommandListener;
import com.ashblossom.farmersdelight.listeners.BlockInteractListener;
import com.ashblossom.farmersdelight.listeners.CropListener;
import com.ashblossom.farmersdelight.listeners.FoodConsumeListener;
import com.ashblossom.farmersdelight.listeners.PlayerJoinListener;
import com.ashblossom.farmersdelight.recipes.FDRecipes;
import com.ashblossom.farmersdelight.resourcepack.PackNettyInjector;
import com.ashblossom.farmersdelight.resourcepack.ResourcePackServer;
import org.bukkit.plugin.java.JavaPlugin;

public class FarmersDelightPlugin extends JavaPlugin {

    private static FarmersDelightPlugin instance;
    private ResourcePackServer resourcePackServer;
    private CookingPotManager cookingPotManager;
    private CuttingBoardManager cuttingBoardManager;
    private SkilletManager skilletManager;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Initialising Farmer's Delight...");
        saveDefaultConfig();

        resourcePackServer = new ResourcePackServer(this);
        resourcePackServer.start();
        PackNettyInjector.inject(this);

        FDRecipes.register(this);

        cookingPotManager = new CookingPotManager(this);
        cuttingBoardManager = new CuttingBoardManager(this);
        skilletManager = new SkilletManager();
        CropManager.init(this);

        getServer().getPluginManager().registerEvents(
            new BlockInteractListener(this, cookingPotManager, cuttingBoardManager, skilletManager), this);
        getServer().getPluginManager().registerEvents(new CropListener(this), this);
        getServer().getPluginManager().registerEvents(new FoodConsumeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, resourcePackServer), this);
        getServer().getPluginManager().registerEvents(new AdminCommandListener(this), this);

        getLogger().info("Farmer's Delight ready. " + com.ashblossom.farmersdelight.items.FDItems.values().length + " items registered.");
    }

    @Override
    public void onDisable() {
        PackNettyInjector.remove();
        if (resourcePackServer != null) resourcePackServer.stop();
        if (cookingPotManager != null) cookingPotManager.saveAll();
    }

    public static FarmersDelightPlugin get() { return instance; }
    public CookingPotManager getCookingPotManager() { return cookingPotManager; }
    public CuttingBoardManager getCuttingBoardManager() { return cuttingBoardManager; }
    public ResourcePackServer getResourcePackServer() { return resourcePackServer; }
}
