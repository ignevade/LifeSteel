package me.evade.LifeSteel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class CommandHandler implements CommandExecutor {

    private final LifeSteel plugin;
    private final BannedPlayersManager bannedPlayersManager;

    public CommandHandler(LifeSteel plugin, BannedPlayersManager bannedPlayersManager) {
        this.plugin = plugin;
        this.bannedPlayersManager = bannedPlayersManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String cmdName = cmd.getName().toLowerCase();

        switch (cmdName) {
            case "setmaxhealth":
                return handleSetMaxHealth(sender, args);
            case "giverevivebeacon":
                return handleGiveReviveBeacon(sender, args);
            case "giveheart":
                return handleGiveHeart(sender, args);
            case "reviveplayer":
                return handleRevivePlayer(sender, args);
            case "revivebeacon":
                return handleReviveBeaconRecipe(sender);
            case "heart":
                return handleHeartRecipe(sender);
            default:
                return false;
        }
    }

    private boolean handleSetMaxHealth(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteel.sethealth")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setmaxhealth <player> <health>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        double health;
        try {
            health = Double.parseDouble(args[1]);
            // Minecraft displays health as hearts, with each heart representing 2 health points
            if (health <= 0) {
                sender.sendMessage(ChatColor.RED + "Health must be greater than 0.");
                return true;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid health value.");
            return true;
        }

        target.setMaxHealth(health);
        target.setHealth(Math.min(target.getHealth(), health)); // Ensure current health doesn't exceed max health

        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s max health to " + health + " (" + (health/2) + " hearts).");
        target.sendMessage(ChatColor.GREEN + "Your max health has been set to " + (health/2) + " hearts.");

        return true;
    }

    private boolean handleGiveReviveBeacon(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteel.giverevivebeacon")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /giverevivebeacon <player> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount.");
                return true;
            }
        }

        ItemStack beacon = createReviveBeacon(amount);

        // Give the player the beacon
        if (target.getInventory().firstEmpty() == -1) {
            // If inventory is full, drop at player's location
            target.getWorld().dropItemNaturally(target.getLocation(), beacon);
            sender.sendMessage(ChatColor.YELLOW + "Player's inventory was full. The beacon was dropped at their location.");
            target.sendMessage(ChatColor.YELLOW + "Your inventory was full. The Revive Beacon was dropped at your location.");
        } else {
            target.getInventory().addItem(beacon);
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " Revive Beacon(s) to " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "You received " + amount + " Revive Beacon(s).");
        }

        return true;
    }

    private boolean handleGiveHeart(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteel.giveheart")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /giveheart <player> [amount]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) {
                    sender.sendMessage(ChatColor.RED + "Amount must be greater than 0.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid amount.");
                return true;
            }
        }

        ItemStack heart = createHeart(amount);

        // Give the player the heart
        if (target.getInventory().firstEmpty() == -1) {
            // If inventory is full, drop at player's location
            target.getWorld().dropItemNaturally(target.getLocation(), heart);
            sender.sendMessage(ChatColor.YELLOW + "Player's inventory was full. The heart(s) were dropped at their location.");
            target.sendMessage(ChatColor.YELLOW + "Your inventory was full. The heart(s) were dropped at your location.");
        } else {
            target.getInventory().addItem(heart);
            sender.sendMessage(ChatColor.GREEN + "Gave " + amount + " heart(s) to " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "You received " + amount + " heart(s).");
        }

        return true;
    }

    private boolean handleRevivePlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lifesteel.reviveplayer")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /reviveplayer <player>");
            return true;
        }

        String targetName = args[0];
        UUID targetUUID = bannedPlayersManager.getUUIDFromName(targetName);

        if (targetUUID == null) {
            sender.sendMessage(ChatColor.RED + "Player " + targetName + " is not banned or does not exist!");
            return true;
        }

        // Revive the player
        bannedPlayersManager.revivePlayer(targetUUID);

        // Add to recently revived players to set their health when they log in
        plugin.addRevivedPlayer(targetUUID);

        // Only set max health if player is online
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null && target.isOnline()) {
            double revivedMaxHealth = plugin.getCustomConfig().getDouble("revived-max-health", 3.0);
            revivedMaxHealth = revivedMaxHealth * 2;
            target.setMaxHealth(revivedMaxHealth);
            target.setHealth(revivedMaxHealth);
            target.sendMessage(ChatColor.GREEN + "You have been revived by an admin! Your max health has been set to " + (revivedMaxHealth/2) + " hearts.");
        }

        // Announce the revival
        String adminName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&a" + adminName + " has revived " + targetName + "!"));

        return true;
    }

    private boolean handleReviveBeaconRecipe(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        Inventory recipeGUI = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Revive Beacon Recipe");

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            recipeGUI.setItem(i, filler);
        }

        // The actual recipe shown in the image
        // Top row
        recipeGUI.setItem(10, createGUIItem(Material.EMERALD_BLOCK, "Emerald Block"));
        recipeGUI.setItem(11, createGUIItem(Material.NETHERITE_INGOT, "Netherite Ingot"));
        recipeGUI.setItem(12, createGUIItem(Material.EMERALD_BLOCK, "Emerald Block"));

        // Middle row
        recipeGUI.setItem(19, createGUIItem(Material.NETHERITE_INGOT, "Netherite Ingot"));
        recipeGUI.setItem(20, createGUIItem(Material.BEACON, "Beacon"));
        recipeGUI.setItem(21, createGUIItem(Material.NETHERITE_INGOT, "Netherite Ingot"));

        // Bottom row
        recipeGUI.setItem(19+9, createGUIItem(Material.EMERALD_BLOCK, "Emerald Block"));
        recipeGUI.setItem(20+9, createGUIItem(Material.NETHERITE_INGOT, "Netherite Ingot"));
        recipeGUI.setItem(21+9, createGUIItem(Material.EMERALD_BLOCK, "Emerald Block"));

        recipeGUI.setItem(15, createReviveBeacon(1));

        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.setDisplayName(ChatColor.GREEN + "→");
        arrow.setItemMeta(arrowMeta);
        recipeGUI.setItem(14, arrow);

        player.openInventory(recipeGUI);

        return true;
    }

    private boolean handleHeartRecipe(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        Inventory recipeGUI = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Heart Recipe");

        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < 27; i++) {
            recipeGUI.setItem(i, filler);
        }

        // Corners - Diamond Blocks
        recipeGUI.setItem(1, createGUIItem(Material.DIAMOND_BLOCK, "Diamond Block"));
        recipeGUI.setItem(3, createGUIItem(Material.DIAMOND_BLOCK, "Diamond Block"));
        recipeGUI.setItem(19, createGUIItem(Material.DIAMOND_BLOCK, "Diamond Block"));
        recipeGUI.setItem(21, createGUIItem(Material.DIAMOND_BLOCK, "Diamond Block"));

        // Middle - Wither Rose
        recipeGUI.setItem(11, createGUIItem(Material.WITHER_ROSE, "Wither Rose"));

        // Other slots - Netherite Scraps
        recipeGUI.setItem(2, createGUIItem(Material.NETHERITE_SCRAP, "Netherite Scrap"));
        recipeGUI.setItem(10, createGUIItem(Material.NETHERITE_SCRAP, "Netherite Scrap"));
        recipeGUI.setItem(12, createGUIItem(Material.NETHERITE_SCRAP, "Netherite Scrap"));
        recipeGUI.setItem(20, createGUIItem(Material.NETHERITE_SCRAP, "Netherite Scrap"));

        // Result
        recipeGUI.setItem(16, createHeart(1));

        // Arrow
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        arrowMeta.setDisplayName(ChatColor.GREEN + "→");
        arrow.setItemMeta(arrowMeta);
        recipeGUI.setItem(14, arrow);

        player.openInventory(recipeGUI);

        return true;
    }

    private ItemStack createGUIItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createReviveBeacon(int amount) {
        ItemStack beacon = new ItemStack(Material.BEACON, amount);
        ItemMeta meta = beacon.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Revive Beacon");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "A powerful beacon that can revive banned players.",
                ChatColor.GRAY + "Right-click to use."
        ));
        beacon.setItemMeta(meta);
        return beacon;
    }

    private ItemStack createHeart(int amount) {
        // Get heart increase amount from the plugin's config
        double heartIncreaseAmount = plugin.getConfig().getDouble("heart-increase-amount", 2.0);
        double heartIncreaseHearts = heartIncreaseAmount / 2.0; // Convert health points to hearts

        ItemStack heart = new ItemStack(Material.RED_DYE, amount);
        ItemMeta meta = heart.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Heart");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Consume to increase your max health by " + heartIncreaseHearts + " heart" +
                        (heartIncreaseHearts != 1.0 ? "s" : "") + "."
        ));
        heart.setItemMeta(meta);
        return heart;
    }
}