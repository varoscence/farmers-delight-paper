package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.crops.CropManager;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockGrowEvent;

public class CropListener implements Listener {

    public CropListener(FarmersDelightPlugin plugin) {}

    @EventHandler(priority = EventPriority.HIGH)
    public void onGrow(BlockGrowEvent event) {
        if (CropManager.isFDCrop(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
