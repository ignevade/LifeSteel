package me.evade.LifeSteel;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
public class LifeSteel extends JavaPlugin {
    private BannedPlayersManager bannedPlayersManager;
    private Set<UUID> revivedPlayers;
    private File customConfigFile;
    private FileConfiguration customConfig;
    @Override
    public void onEnable() {
        DefaultConfigHandler configHandler = new DefaultConfigHandler(this);
        configHandler.createDefaultConfig();
        reloadConfig();
        configHandler.updateConfig();
        createCustomConfig();
        bannedPlayersManager = new BannedPlayersManager(this, customConfigFile);
        revivedPlayers = new HashSet<>();
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, bannedPlayersManager), this);
        getServer().getPluginManager().registerEvents(new ReviveBeaconListener(this, bannedPlayersManager), this);
        getServer().getPluginManager().registerEvents(new GuiHandler(this), this);
        getServer().getPluginManager().registerEvents(new HeartHandler(this), this);
        CommandHandler commandHandler = new CommandHandler(this, bannedPlayersManager);
        getCommand("setmaxhealth").setExecutor(commandHandler);
        getCommand("giverevivebeacon").setExecutor(commandHandler);
        getCommand("giveheart").setExecutor(commandHandler);
        getCommand("reviveplayer").setExecutor(commandHandler);
        getCommand("revivebeacon").setExecutor(commandHandler);
        getCommand("heart").setExecutor(commandHandler);
        registerReviveBeaconRecipe();
        registerHeartRecipe();
        getLogger().info("LifeSteel has been enabled!");
    }
    @Override
    public void onDisable() {
        if (customConfig != null) {
            try {
                customConfig.save(customConfigFile);
                getLogger().info("Saved banned players data.");
            } catch (IOException e) {
                getLogger().severe("Could not save bannedplayers.yml: " + e.getMessage());
            }
        }
        getLogger().info("LifeSteel has been disabled!");
    }
    public void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "bannedplayers.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            try {
                customConfigFile.createNewFile();
                getLogger().info("Created new bannedplayers.yml file");
            } catch (IOException e) {
                getLogger().severe("Could not create bannedplayers.yml: " + e.getMessage());
            }
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }
    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }
    public void saveCustomConfig() {
        if (customConfig == null || customConfigFile == null) {
            getLogger().warning("Cannot save config because it was never loaded");
            return;
        }
        try {
            customConfig.save(customConfigFile);
        } catch (IOException e) {
            getLogger().severe("Could not save bannedplayers.yml: " + e.getMessage());
        }
    }
    private void registerReviveBeaconRecipe() {
        ItemStack reviveBeacon = new ItemStack(Material.BEACON);
        ItemMeta meta = reviveBeacon.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Revive Beacon");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "A powerful beacon that can revive banned players.");
        lore.add(ChatColor.GRAY + "Right-click to use.");
        meta.setLore(lore);
        reviveBeacon.setItemMeta(meta);
        NamespacedKey key = new NamespacedKey(this, "revive_beacon");
        ShapedRecipe recipe = new ShapedRecipe(key, reviveBeacon);
        recipe.shape("ENE", "NBN", "ENE");
        recipe.setIngredient('E', Material.EMERALD_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_INGOT);
        recipe.setIngredient('B', Material.BEACON);
        getServer().addRecipe(recipe);
        getLogger().info("Registered Revive Beacon recipe");
    }
    private void registerHeartRecipe() {
        double heartIncreaseAmount = getConfig().getDouble("heart-increase-amount", 2.0);
        double heartIncreaseHearts = heartIncreaseAmount / 2.0;
        ItemStack heart = new ItemStack(Material.RED_DYE);
        ItemMeta meta = heart.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Heart");
        meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Consume to increase your max health by " + heartIncreaseHearts + " heart" +
                        (heartIncreaseHearts != 1.0 ? "s" : "") + "."
        ));
        heart.setItemMeta(meta);
        NamespacedKey key = new NamespacedKey(this, "heart");
        ShapedRecipe recipe = new ShapedRecipe(key, heart);
        recipe.shape("DND", "NWN", "DND");
        recipe.setIngredient('D', Material.DIAMOND_BLOCK);
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('W', Material.WITHER_ROSE);
        getServer().addRecipe(recipe);
        getLogger().info("Registered Heart recipe");
    }
    public void addRevivedPlayer(UUID uuid) {
        revivedPlayers.add(uuid);
    }
    public void removeRevivedPlayer(UUID uuid) {
        revivedPlayers.remove(uuid);
    }
    public boolean isRevivedPlayer(UUID uuid) {
        return revivedPlayers.contains(uuid);
    }
}