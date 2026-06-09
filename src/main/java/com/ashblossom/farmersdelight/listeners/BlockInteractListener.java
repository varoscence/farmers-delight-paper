package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.blocks.CookingPotManager;
import com.ashblossom.farmersdelight.blocks.CuttingBoardManager;
import com.ashblossom.farmersdelight.crops.CropManager;
import com.ashblossom.farmersdelight.items.FDItems;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;

public class BlockInteractListener implements Listener {

    private final FarmersDelightPlugin plugin;
    private final CookingPotManager pots;
    private final CuttingBoardManager boards;

    public BlockInteractListener(FarmersDelightPlugin plugin, CookingPotManager pots, CuttingBoardManager boards) {
        this.plugin = plugin;
        this.pots = pots;
        this.boards = boards;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        ItemStack hand = event.getItem();
        FDItems fdHeld = (hand != null) ? FDItems.fromStack(hand) : null;

        // ── Place Cooking Pot ──────────────────────────────────────────────
        if (fdHeld == FDItems.COOKING_POT_ITEM && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block target = block.getRelative(event.getBlockFace());
            if (target.getType() == Material.AIR) {
                target.setType(Material.CAULDRON);
                pots.placePot(target.getLocation());
                hand.setAmount(hand.getAmount() - 1);
                event.setCancelled(true);
                return;
            }
        }

        // ── Place Cutting Board ───────────────────────────────────────────
        if (fdHeld == FDItems.CUTTING_BOARD_ITEM && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block target = block.getRelative(event.getBlockFace());
            if (target.getType() == Material.AIR) {
                target.setType(Material.OAK_SLAB);
                Slab slab = (Slab) target.getBlockData();
                slab.setType(Slab.Type.BOTTOM);
                target.setBlockData(slab);
                boards.placeBoard(target.getLocation());
                hand.setAmount(hand.getAmount() - 1);
                event.setCancelled(true);
                return;
            }
        }

        // ── Open Cooking Pot ───────────────────────────────────────────────
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && pots.isCookingPot(block)) {
            event.setCancelled(true);
            pots.openPot(player, block.getLocation());
            return;
        }

        // ── Cutting Board: place item ──────────────────────────────────────
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && boards.isCuttingBoard(block)) {
            event.setCancelled(true);
            if (hand != null && hand.getType() != Material.AIR && !FDItems.isKnife(hand)) {
                if (!boards.hasItem(block.getLocation())) {
                    boards.placeItem(block.getLocation(), hand.asOne());
                    hand.setAmount(hand.getAmount() - 1);
                }
            }
            return;
        }

        // ── Cutting Board: use knife ───────────────────────────────────────
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && boards.isCuttingBoard(block)) {
            if (FDItems.isKnife(hand)) {
                event.setCancelled(true);
                boards.tryCut(block.getLocation(), player);
            }
            return;
        }

        // ── FD Crop planting ──────────────────────────────────────────────
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK
            && block.getType() == Material.FARMLAND && fdHeld != null) {
            if (CropManager.CROP_BLOCKS.containsKey(fdHeld)) {
                event.setCancelled(true);
                CropManager.tryPlant(player, block, fdHeld);
            }
            return;
        }

        // ── FD Crop harvesting ────────────────────────────────────────────
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && CropManager.isFDCrop(block)) {
            event.setCancelled(true);
            CropManager.tryHarvest(player, block);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (pots.isCookingPot(block)) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5),
                FDItems.COOKING_POT_ITEM.create());
            pots.breakPot(block.getLocation());
        } else if (boards.isCuttingBoard(block)) {
            event.setDropItems(false);
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5),
                FDItems.CUTTING_BOARD_ITEM.create());
            boards.breakBoard(block.getLocation());
        } else if (CropManager.isFDCrop(block)) {
            event.setCancelled(true);
            CropManager.tryHarvest(event.getPlayer(), block);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().title().equals(Component.text("Cooking Pot",
            net.kyori.adventure.text.format.NamedTextColor.DARK_RED))) {
            // find which pot this player has open — iterate loaded worlds/chunks is expensive,
            // so we use a simpler "check nearby blocks" approach
            Block target = player.getTargetBlockExact(5);
            if (target != null && pots.isCookingPot(target)) {
                pots.handleGuiClick(player, event, target.getLocation());
                if (event.getSlot() == 11 && event.getCurrentItem() != null
                    && event.getCurrentItem().getType() != Material.AIR) {
                    plugin.getServer().getScheduler().runTask(plugin, () ->
                        pots.onOutputTaken(player, target.getLocation(), event.getInventory()));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getView().title().equals(Component.text("Cooking Pot",
            net.kyori.adventure.text.format.NamedTextColor.DARK_RED))) {
            Block target = player.getTargetBlockExact(5);
            if (target != null && pots.isCookingPot(target)) {
                pots.onClose(target.getLocation(), event.getInventory());
            }
        }
    }
}
