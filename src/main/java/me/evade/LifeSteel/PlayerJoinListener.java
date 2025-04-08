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
    public PlayerJoinListener(LifeSteel plugin, BannedPlayersManager bannedPlayersManager) {this.plugin = plugin;
        this.bannedPlayersManager = bannedPlayersManager;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (bannedPlayersManager.isPlayerBanned(player.getUniqueId())) {
            event.setJoinMessage(null);
            player.setMetadata(BANNED_METADATA, new FixedMetadataValue(plugin, true));
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
        if (plugin.isRevivedPlayer(player.getUniqueId())) {
            double revivedMaxHealth = plugin.getCustomConfig().getDouble("revived-max-health", 3.0);
            revivedMaxHealth = revivedMaxHealth * 2;
            player.setMaxHealth(revivedMaxHealth);
            player.setHealth(revivedMaxHealth);
            player.sendMessage(ChatColor.GREEN + "You have been revived! Your max health has been set to " + (revivedMaxHealth/2) + " hearts.");
            plugin.removeRevivedPlayer(player.getUniqueId());
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.hasMetadata(BANNED_METADATA)) {
            event.setQuitMessage(null);
            player.removeMetadata(BANNED_METADATA, plugin);
        }
    }
}