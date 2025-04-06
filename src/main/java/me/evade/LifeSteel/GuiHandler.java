package me.evade.LifeSteel;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.ChatColor;

public class GuiHandler implements Listener {

    private final LifeSteel plugin;

    public GuiHandler(LifeSteel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        // Cancel clicks in recipe GUIs
        if (title.equals(ChatColor.DARK_PURPLE + "Revive Beacon Recipe") ||
                title.equals(ChatColor.DARK_RED + "Heart Recipe")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();

        // Cancel drags in recipe GUIs
        if (title.equals(ChatColor.DARK_PURPLE + "Revive Beacon Recipe") ||
                title.equals(ChatColor.DARK_RED + "Heart Recipe")) {
            event.setCancelled(true);
        }
    }
}