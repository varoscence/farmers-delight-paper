package com.ashblossom.farmersdelight.listeners;

import com.ashblossom.farmersdelight.FarmersDelightPlugin;
import com.ashblossom.farmersdelight.resourcepack.ResourcePackServer;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private final FarmersDelightPlugin plugin;
    private final ResourcePackServer packServer;

    public PlayerJoinListener(FarmersDelightPlugin plugin, ResourcePackServer packServer) {
        this.plugin = plugin;
        this.packServer = packServer;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            String url = packServer.getUrl();
            byte[] hash = packServer.getHash();
            if (url == null || hash == null) return;
            player.setResourcePack(UUID.randomUUID(), url, hash,
                net.kyori.adventure.text.Component.text("Farmer's Delight resource pack"), true);
        }, 20L);
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case FAILED_DOWNLOAD, FAILED_RELOAD ->
                plugin.getLogger().warning("Pack failed for " + event.getPlayer().getName());
            case SUCCESSFULLY_LOADED ->
                plugin.getLogger().info("Pack loaded for " + event.getPlayer().getName());
            default -> {}
        }
    }
}
