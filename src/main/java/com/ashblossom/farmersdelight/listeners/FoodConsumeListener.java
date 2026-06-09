package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.items.FDItems;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class FoodConsumeListener implements Listener {

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item.getType() == Material.MILK_BUCKET) {
            giveBack(player, new ItemStack(Material.GLASS_BOTTLE));
            return;
        }

        FDItems fdItem = FDItems.fromStack(item);
        if (fdItem == null) return;

        if (fdItem.getItemType() == FDItems.ItemType.BOWL_FOOD) {
            giveBack(player, new ItemStack(Material.BOWL));
        } else if (fdItem.getItemType() == FDItems.ItemType.DRINK) {
            giveBack(player, new ItemStack(Material.GLASS_BOTTLE));
        }
    }

    private void giveBack(Player player, ItemStack container) {
        FarmersDelightPlugin.get().getServer().getScheduler().runTask(
            FarmersDelightPlugin.get(),
            () -> {
                ItemStack main = player.getInventory().getItemInMainHand();
                if (main.getType() == Material.AIR) {
                    player.getInventory().setItemInMainHand(container);
                } else {
                    player.getInventory().addItem(container);
                }
            });
    }
}
