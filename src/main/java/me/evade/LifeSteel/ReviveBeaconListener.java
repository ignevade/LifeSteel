package me.evade.LifeSteel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReviveBeaconListener implements Listener {
    private final LifeSteel plugin;
    private final BannedPlayersManager bannedPlayersManager;
    private final Map<UUID, Boolean> waitingForInput;
    private final Map<UUID, ItemStack> beaconStorage;

    public ReviveBeaconListener(LifeSteel plugin, BannedPlayersManager bannedPlayersManager) {
        this.plugin = plugin;
        this.bannedPlayersManager = bannedPlayersManager;
        this.waitingForInput = new HashMap<>();
        this.beaconStorage = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !isReviveBeacon(item)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the player is already selecting a name
            if (waitingForInput.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You're already in the process of reviving a player!");
                event.setCancelled(true);
                return;
            }

            // Check if any player is currently selecting
            if (!waitingForInput.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Someone else is currently using a Revive Beacon. Please wait until they finish.");
                event.setCancelled(true);
                return;
            }

            // Create a single beacon item copy (just 1, not the entire stack)
            ItemStack beaconCopy = item.clone();
            beaconCopy.setAmount(1);  // Ensure we only store ONE beacon regardless of stack size

            // Take away ONE beacon immediately
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            // Store the player's UUID and beacon copy for potential cancellation
            waitingForInput.put(player.getUniqueId(), true);
            beaconStorage.put(player.getUniqueId(), beaconCopy);

            player.sendMessage(ChatColor.YELLOW + "Please input the name of the player you want to revive, to cancel type 'cancel'");

            // Prevent placing the beacon
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();

        if (!waitingForInput.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        waitingForInput.remove(player.getUniqueId());

        final String targetName = event.getMessage();
        final ItemStack beaconItem = beaconStorage.remove(player.getUniqueId());

        // Run the item processing on the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                // Return the beacon if cancelled
                if (targetName.equalsIgnoreCase("cancel")) {
                    player.sendMessage(ChatColor.RED + "Revive beacon process canceled.");

                    if (beaconItem != null) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(beaconItem);

                        // If there's no space in inventory, drop it on the ground
                        if (!leftover.isEmpty()) {
                            for (ItemStack item : leftover.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            player.sendMessage(ChatColor.YELLOW + "Your inventory was full. The beacon was dropped on the ground.");
                        }
                    }
                    return;
                }

                UUID targetUUID = bannedPlayersManager.getUUIDFromName(targetName);

                if (targetUUID == null) {
                    player.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned or does not exist!");

                    // Return the beacon since the revival failed
                    if (beaconItem != null) {
                        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(beaconItem);

                        // If there's no space in inventory, drop it on the ground
                        if (!leftover.isEmpty()) {
                            for (ItemStack item : leftover.values()) {
                                player.getWorld().dropItemNaturally(player.getLocation(), item);
                            }
                            player.sendMessage(ChatColor.YELLOW + "Your inventory was full. The beacon was dropped on the ground.");
                        }
                    }
                    return;
                }
                bannedPlayersManager.revivePlayer(targetUUID);
                plugin.addRevivedPlayer(targetUUID);
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a" + player.getName() + " has revived " + targetName + "!"));
            }
        }.runTask(plugin); // This runs the code in the main server thread
    }

    private boolean isReviveBeacon(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        return ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase("Revive Beacon");
    }
}