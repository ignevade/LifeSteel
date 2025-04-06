package me.evade.LifeSteel;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.event.block.Action;
import org.bukkit.Bukkit;

public class LifestealListener implements Listener {

    private final LifeSteel plugin;
    private final BannedPlayersManager bannedPlayersManager;

    public LifestealListener(LifeSteel plugin, BannedPlayersManager bannedPlayersManager) {
        this.plugin = plugin;
        this.bannedPlayersManager = bannedPlayersManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null || killer.equals(victim)) {
            return;
        }

        double maxHealth = victim.getMaxHealth();
        FileConfiguration config = plugin.getCustomConfig();
        double minHealthBeforeBan = config.getDouble("min-health-before-ban", 2.0);
        double healthDecreaseOnDeath = config.getDouble("health-decrease-on-death", 2.0);

        if (maxHealth > minHealthBeforeBan) {
            victim.setMaxHealth(maxHealth - healthDecreaseOnDeath);

            ItemStack heart = createHeart(healthDecreaseOnDeath);
            victim.getWorld().dropItemNaturally(victim.getLocation(), heart);

            // Check if player needs to be banned
            if (victim.getMaxHealth() <= minHealthBeforeBan) {
                bannedPlayersManager.banPlayer(victim.getUniqueId(), victim.getName());

                // Schedule kick after a short delay to allow death animations to complete
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isOnline()) {
                        victim.kickPlayer(ChatColor.translateAlternateColorCodes('&',
                                "&cYou have been banned!\n&rAnother member must revive you using a &bRevive Beacon &rfor you to come back!"));
                    }
                }, 20L); // 1 second delay
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !isHeart(item)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            double maxHealth = player.getMaxHealth();
            FileConfiguration config = plugin.getCustomConfig();
            double maxAllowedHealth = config.getDouble("max-health", 40.0);
            double healthIncreasePerHeart = config.getDouble("health-increase-per-heart", 2.0);

            if (maxHealth >= maxAllowedHealth) {
                player.sendMessage(ChatColor.RED + "You already have maximum health!");
                return;
            }

            player.setMaxHealth(maxHealth + healthIncreasePerHeart);

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(ChatColor.GREEN + "You have gained +" + (int)healthIncreasePerHeart + " max health!");
            event.setCancelled(true);
        }
    }

    private ItemStack createHeart(double healthAmount) {
        ItemStack heart = new ItemStack(Material.RED_DYE);
        ItemMeta meta = heart.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cHeart"));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&7Right Click this item to gain +" + (int)healthAmount + " max health"));
        meta.setLore(lore);

        heart.setItemMeta(meta);
        return heart;
    }

    // Overload for backward compatibility
    private ItemStack createHeart() {
        return createHeart(plugin.getCustomConfig().getDouble("health-increase-per-heart", 2.0));
    }

    private boolean isHeart(ItemStack item) {
        if (item.getType() != Material.RED_DYE) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Heart");
    }
}