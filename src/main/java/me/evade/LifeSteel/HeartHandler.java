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
        double healthDecreaseAmount = plugin.getConfig().getDouble("health-decrease-on-death", 2.0);
        double dropChance = plugin.getConfig().getDouble("heart-drop-chance", 0.3);
        if (healthDecreaseAmount > 0 && killer != null && killer instanceof Player && random.nextDouble() <= dropChance) {
            double newMaxHealth = Math.max(player.getMaxHealth() - healthDecreaseAmount, 2.0);
            final double finalHealth = newMaxHealth;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.setMaxHealth(finalHealth);
                player.sendMessage(ChatColor.RED + "Your maximum health has decreased to " +
                        (finalHealth / 2) + " hearts due to death.");
            });
            ItemStack heart = createHeart(1);
            event.getDrops().add(heart);
            if (plugin.getConfig().getBoolean("notify-heart-drop", true)) {
                killer.sendMessage(ChatColor.RED + "The player dropped a heart!");
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUseHeart(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && isHeart(item) &&
                (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            double heartIncreaseAmount = plugin.getConfig().getDouble("heart-increase-amount", 2.0);
            double maxHealthLimit = plugin.getConfig().getDouble("max-health-limit", 20.0);
            maxHealthLimit = maxHealthLimit * 2;
            if (player.getMaxHealth() >= maxHealthLimit) {
                player.sendMessage(ChatColor.RED + "You've already reached the maximum heart limit!");
                return;
            }
            double newMaxHealth = Math.min(player.getMaxHealth() + heartIncreaseAmount, maxHealthLimit);
            player.setMaxHealth(newMaxHealth);
            if (plugin.getConfig().getBoolean("heal-on-heart-use", true)) {
                double newHealth = Math.min(player.getHealth() + heartIncreaseAmount, newMaxHealth);
                player.setHealth(newHealth);
            }
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
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
        double heartIncreaseAmount = plugin.getConfig().getDouble("heart-increase-amount", 2.0);
        double heartIncreaseHearts = heartIncreaseAmount / 2.0;
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