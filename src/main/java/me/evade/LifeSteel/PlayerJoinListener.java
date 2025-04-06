package me.evade.LifeSteel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    private final LifeSteel plugin;
    private final BannedPlayersManager bannedPlayersManager;
    private static final String BANNED_METADATA = "lifesteel_banned";

    public PlayerJoinListener(LifeSteel plugin, BannedPlayersManager bannedPlayersManager) {
        this.plugin = plugin;
        this.bannedPlayersManager = bannedPlayersManager;
    }

    @EventHandler(priority = EventPriority.LOWEST) // Use LOWEST to handle before other plugins
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player was banned
        if (bannedPlayersManager.isPlayerBanned(player.getUniqueId())) {
            // Set join message to empty to prevent the "Player joined" message
            event.setJoinMessage(null);

            // Mark the player with metadata so we can cancel their quit message later
            player.setMetadata(BANNED_METADATA, new FixedMetadataValue(plugin, true));

            // Schedule the kick for the next tick to ensure the player sees the message
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                "&cYou have been banned!\n&rAnother member must revive you using a &bRevive Beacon &rfor you to come back!"));
                    }
                }
            }.runTaskLater(plugin, 1L);
            return;
        }

        // Check if player was recently revived but was offline
        if (plugin.isRevivedPlayer(player.getUniqueId())) {
            // Set the player's max health from the config
            double revivedMaxHealth = plugin.getCustomConfig().getDouble("revived-max-health", 3.0);
            revivedMaxHealth = revivedMaxHealth * 2;
            player.setMaxHealth(revivedMaxHealth);
            player.setHealth(revivedMaxHealth);
            player.sendMessage(ChatColor.GREEN + "You have been revived! Your max health has been set to " + (revivedMaxHealth/2) + " hearts.");

            // Remove from the revived players list after setting the health
            plugin.removeRevivedPlayer(player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if this player was marked as banned
        if (player.hasMetadata(BANNED_METADATA)) {
            // Suppress the quit message
            event.setQuitMessage(null);

            // Clean up the metadata
            player.removeMetadata(BANNED_METADATA, plugin);
        }
    }
}