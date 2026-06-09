package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.blocks.CookingPotManager;
import com.ashblossom.farmersdelight.blocks.CuttingBoardManager;
import com.ashblossom.farmersdelight.crops.CropManager;
import com.ashblossom.farmersdelight.gui.FDInventoryHolder;
import com.ashblossom.farmersdelight.gui.GuideBookGui;
import com.ashblossom.farmersdelight.gui.RecipeViewerGui;
import com.ashblossom.farmersdelight.items.FDItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        Player player = event.getPlayer();
        ItemStack hand = event.getItem();

        // ── Farmer's Guide book (works in air and on block) ─────────────────
        if (GuideBookGui.isGuideBook(hand)
                && (event.getAction() == Action.RIGHT_CLICK_AIR
                    || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            GuideBookGui.open(player);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
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

        // ── Custom GUI (guide book + recipe viewers) ───────────────────────
        if (event.getInventory().getHolder() instanceof FDInventoryHolder holder) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null) return;
            if (!event.getInventory().equals(event.getClickedInventory())) return;
            int slot = event.getSlot();
            switch (holder.type) {
                case GUIDE_MAIN -> handleGuideClick(player, slot);
                case COOKING_RECIPES -> handleViewerClick(player, slot, holder.page,
                    FDInventoryHolder.GuiType.COOKING_RECIPES);
                case CUTTING_RECIPES -> handleViewerClick(player, slot, holder.page,
                    FDInventoryHolder.GuiType.CUTTING_RECIPES);
            }
            return;
        }

        // ── Cooking Pot ────────────────────────────────────────────────────
        if (event.getView().title().equals(Component.text("Cooking Pot", NamedTextColor.DARK_RED))) {
            Block target = player.getTargetBlockExact(5);
            if (target != null && pots.isCookingPot(target)) {
                pots.handleGuiClick(player, event, target.getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getView().title().equals(Component.text("Cooking Pot", NamedTextColor.DARK_RED))) {
            Block target = player.getTargetBlockExact(5);
            if (target != null && pots.isCookingPot(target)) {
                pots.onClose(target.getLocation(), event.getInventory());
            }
        }
    }

    private void handleGuideClick(Player player, int slot) {
        switch (slot) {
            case 11 -> RecipeViewerGui.openCooking(player, 0);
            case 13 -> RecipeViewerGui.openCutting(player, 0);
            case 15 -> player.sendMessage(Component.text(
                "Open your crafting table and click the recipe book icon (bottom-left)!")
                .color(NamedTextColor.YELLOW));
        }
    }

    private void handleViewerClick(Player player, int slot, int page, FDInventoryHolder.GuiType type) {
        if (slot == 45) { GuideBookGui.open(player); return; }
        if (slot == 48) {
            if (type == FDInventoryHolder.GuiType.COOKING_RECIPES) RecipeViewerGui.openCooking(player, page - 1);
            else RecipeViewerGui.openCutting(player, page - 1);
            return;
        }
        if (slot == 50) {
            if (type == FDInventoryHolder.GuiType.COOKING_RECIPES) RecipeViewerGui.openCooking(player, page + 1);
            else RecipeViewerGui.openCutting(player, page + 1);
        }
    }
}
