package me.evade.LifeSteel;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class HeartHandler implements Listener {

    private final LifeSteel plugin;
    private final Random random = new Random();

    public HeartHandler(LifeSteel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        // Decrease player's max health on death
        double healthDecreaseAmount = plugin.getConfig().getDouble("health-decrease-on-death", 2.0);

        // Check config for drop chance
        double dropChance = plugin.getConfig().getDouble("heart-drop-chance", 0.3); // 30% by default

        // Only drop a heart if it's a PVP kill and random chance is met
        if (healthDecreaseAmount > 0 && killer != null && killer instanceof Player && random.nextDouble() <= dropChance) {
            double newMaxHealth = Math.max(player.getMaxHealth() - healthDecreaseAmount, 2.0);

            // We'll set the new max health when the player respawns to avoid issues
            final double finalHealth = newMaxHealth;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.setMaxHealth(finalHealth);
                player.sendMessage(ChatColor.RED + "Your maximum health has decreased to " +
                        (finalHealth / 2) + " hearts due to death.");
            });
            ItemStack heart = createHeart(1);

            // Add to drops
            event.getDrops().add(heart);

            // Optionally notify the killer
            if (plugin.getConfig().getBoolean("notify-heart-drop", true)) {
                killer.sendMessage(ChatColor.RED + "The player dropped a heart!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUseHeart(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the player right-clicked with a heart
        if (item != null && isHeart(item) &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {

            event.setCancelled(true); // Cancel the event to prevent normal interaction

            // Get the max heart increase amount from config
            double heartIncreaseAmount = plugin.getConfig().getDouble("heart-increase-amount", 2.0); // 1 heart = 2 health points

            // Get max health limit from config
            double maxHealthLimit = plugin.getConfig().getDouble("max-health-limit", 20.0); // 20 hearts by default
            maxHealthLimit = maxHealthLimit * 2;
            // Check if player is already at max health
            if (player.getMaxHealth() >= maxHealthLimit) {
                player.sendMessage(ChatColor.RED + "You've already reached the maximum heart limit!");
                return;
            }

            // Calculate new max health
            double newMaxHealth = Math.min(player.getMaxHealth() + heartIncreaseAmount, maxHealthLimit);

            // Set new max health
            player.setMaxHealth(newMaxHealth);

            // Heal the player by the increase amount (optional)
            if (plugin.getConfig().getBoolean("heal-on-heart-use", true)) {
                double newHealth = Math.min(player.getHealth() + heartIncreaseAmount, newMaxHealth);
                player.setHealth(newHealth);
            }

            // Consume the heart
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            // Play effects
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // Send message
            player.sendMessage(ChatColor.GREEN + "You used a heart! Your maximum health is now " +
                    (newMaxHealth / 2) + " hearts.");
        }
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

    private ItemStack createHeart(int amount) {
        // Get heart increase amount from config
        double heartIncreaseAmount = plugin.getConfig().getDouble("heart-increase-amount", 2.0);
        double heartIncreaseHearts = heartIncreaseAmount / 2.0; // Convert health points to hearts

        ItemStack heart = new ItemStack(Material.RED_DYE, amount);
        ItemMeta meta = heart.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Heart");
        meta.setLore(java.util.Arrays.asList(
                ChatColor.GRAY + "Consume to increase your max health by " + heartIncreaseHearts + " heart" +
                        (heartIncreaseHearts != 1.0 ? "s" : "") + "."
        ));
        heart.setItemMeta(meta);
        return heart;
    }
}