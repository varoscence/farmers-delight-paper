package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.gui.GuideBookGui;
import com.ashblossom.farmersdelight.items.FDItems;
import com.ashblossom.farmersdelight.resourcepack.ResourcePackServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            case "packstatus" -> {
                ResourcePackServer rps = plugin.getResourcePackServer();
                String url  = rps.getUrl();
                byte[] hash = rps.getHash();
                int size    = rps.getPackBytes().length;
                sender.sendMessage(Component.text("=== FD Pack Status ===", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("URL: " + (url == null ? "NOT SET" : url),
                    url == null ? NamedTextColor.RED : NamedTextColor.GREEN));
                sender.sendMessage(Component.text("Size: " + size + " bytes",
                    size > 1000 ? NamedTextColor.GREEN : NamedTextColor.RED));
                sender.sendMessage(Component.text("Hash: " + (hash != null && hash.length == 20 ? "OK (20 bytes)" : "MISSING"),
                    hash != null && hash.length == 20 ? NamedTextColor.GREEN : NamedTextColor.RED));
                // Self-test: open a socket to localhost and send GET request
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    int port = plugin.getServer().getPort();
                    try (Socket s = new Socket("127.0.0.1", port)) {
                        s.setSoTimeout(3000);
                        OutputStream out = s.getOutputStream();
                        out.write("GET /pack.zip HTTP/1.0\r\nHost: localhost\r\n\r\n"
                            .getBytes(StandardCharsets.US_ASCII));
                        out.flush();
                        byte[] buf = new byte[16];
                        int read = s.getInputStream().read(buf);
                        String resp = read > 0 ? new String(buf, 0, read, StandardCharsets.US_ASCII) : "";
                        boolean ok = resp.startsWith("HTTP/1.1 200");
                        plugin.getServer().getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text(
                                "HTTP self-test: " + (ok ? "PASS — Netty serving pack" : "FAIL — '" + resp.replace("\r","").replace("\n"," ") + "'"),
                                ok ? NamedTextColor.GREEN : NamedTextColor.RED)));
                    } catch (Exception e) {
                        plugin.getServer().getScheduler().runTask(plugin, () ->
                            sender.sendMessage(Component.text("HTTP self-test: FAIL — " + e.getMessage(), NamedTextColor.RED)));
                    }
                });
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
        s.sendMessage(Component.text("/fd packstatus", NamedTextColor.YELLOW));
        s.sendMessage(Component.text("/fd guide", NamedTextColor.YELLOW));
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
