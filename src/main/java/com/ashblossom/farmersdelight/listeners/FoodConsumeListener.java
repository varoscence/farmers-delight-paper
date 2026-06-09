package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FoodConsumeListener implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.MILK_BUCKET) {
            Player player = event.getPlayer();
            FarmersDelightPlugin.get().getServer().getScheduler().runTask(
                FarmersDelightPlugin.get(),
                () -> {
                    ItemStack main = player.getInventory().getItemInMainHand();
                    if (main.getType() == Material.AIR) {
                        player.getInventory().setItemInMainHand(new ItemStack(Material.GLASS_BOTTLE));
                    } else {
                        player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                    }
                });
        }
    }
}
