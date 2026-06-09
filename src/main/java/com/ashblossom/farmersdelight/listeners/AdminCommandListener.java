package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.gui.GuideBookGui;
import com.ashblossom.farmersdelight.items.FDItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class AdminCommandListener implements Listener, CommandExecutor {

    private final FarmersDelightPlugin plugin;

    public AdminCommandListener(FarmersDelightPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("fd").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("farmersdelight.admin")) {
            sender.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "give" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Must be a player.");
                    return true;
                }
                if (args.length < 2) { sender.sendMessage(Component.text("Usage: /fd give <item_id> [amount]", NamedTextColor.YELLOW)); return true; }
                FDItems item = FDItems.byId(args[1]);
                if (item == null) { sender.sendMessage(Component.text("Unknown item: " + args[1], NamedTextColor.RED)); return true; }
                int amount = args.length >= 3 ? parseInt(args[2], 1) : 1;
                ItemStack stack = item.create(amount);
                player.getInventory().addItem(stack);
                player.sendMessage(Component.text("Given " + amount + "x " + item.getId(), NamedTextColor.GREEN));
            }
            case "list" -> {
                StringBuilder sb = new StringBuilder("FD Items:\n");
                for (FDItems item : FDItems.values()) sb.append(" - ").append(item.getId()).append("\n");
                sender.sendMessage(Component.text(sb.toString(), NamedTextColor.AQUA));
            }
            case "reload" -> {
                plugin.getResourcePackServer().rebuildPack();
                sender.sendMessage(Component.text("Resource pack rebuilt.", NamedTextColor.GREEN));
            }
            case "guide" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Must be a player.");
                    return true;
                }
                player.getInventory().addItem(GuideBookGui.createGuideItem());
                player.sendMessage(Component.text("Given: Farmer's Guide", NamedTextColor.GREEN));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender s) {
        s.sendMessage(Component.text("/fd give <item_id> [amount]", NamedTextColor.YELLOW));
        s.sendMessage(Component.text("/fd list", NamedTextColor.YELLOW));
        s.sendMessage(Component.text("/fd reload", NamedTextColor.YELLOW));
        s.sendMessage(Component.text("/fd guide", NamedTextColor.YELLOW));
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
