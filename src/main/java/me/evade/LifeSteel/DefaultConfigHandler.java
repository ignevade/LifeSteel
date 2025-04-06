package me.evade.LifeSteel;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class DefaultConfigHandler {

    private final JavaPlugin plugin;

    public DefaultConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates the default config.yml if it doesn't exist,
     * ensuring all comments and default values are included.
     */
    public void createDefaultConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();

            // Save default config from resources
            try {
                // Create the file
                configFile.createNewFile();

                // Write the default config
                YamlConfiguration config = new YamlConfiguration();

                // Health settings
                config.set("revived-max-health", 6.0);
                config.setComments("revived-max-health", java.util.Collections.singletonList("The max health a player gets when revived (3 hearts)"));

                config.set("max-health-limit", 40.0);
                config.setComments("max-health-limit", java.util.Collections.singletonList("Maximum health a player can reach (20 hearts)"));

                config.set("health-decrease-on-death", 2.0);
                config.setComments("health-decrease-on-death", java.util.Collections.singletonList("Amount of max health lost when a player dies (1 heart)"));

                // Add a comment for the Heart settings section
                config.setComments("heart-drop-chance", java.util.Arrays.asList("", "Heart settings"));

                // Heart settings
                config.set("heart-drop-chance", 1.0);
                config.setComments("heart-drop-chance", java.util.Collections.singletonList("100% chance to drop a heart on PVP kill"));

                config.set("heart-increase-amount", 2.0);
                config.setComments("heart-increase-amount", java.util.Collections.singletonList("Amount of health points added per heart (1 heart)"));

                config.set("heal-on-heart-use", true);
                config.setComments("heal-on-heart-use", java.util.Collections.singletonList("Whether to heal the player when using a heart"));

                config.set("notify-heart-drop", true);
                config.setComments("notify-heart-drop", java.util.Collections.singletonList("Whether to notify the killer that a heart was dropped"));

                // Save the config to file
                config.save(configFile);

                plugin.getLogger().info("Created default config.yml with all settings and comments");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create default config.yml: " + e.getMessage());
            }
        }
    }

    /**
     * Updates an existing config with any new values from the default config,
     * while preserving any settings the user has already configured.
     */
    public void updateConfig() {
        FileConfiguration existingConfig = plugin.getConfig();

        // Set default values if they don't exist in the current config
        if (!existingConfig.contains("revived-max-health"))
            existingConfig.set("revived-max-health", 6.0);

        if (!existingConfig.contains("max-health-limit"))
            existingConfig.set("max-health-limit", 40.0);

        if (!existingConfig.contains("health-decrease-on-death"))
            existingConfig.set("health-decrease-on-death", 2.0);

        if (!existingConfig.contains("heart-drop-chance"))
            existingConfig.set("heart-drop-chance", 1.0);

        if (!existingConfig.contains("heart-increase-amount"))
            existingConfig.set("heart-increase-amount", 2.0);

        if (!existingConfig.contains("heal-on-heart-use"))
            existingConfig.set("heal-on-heart-use", true);

        if (!existingConfig.contains("notify-heart-drop"))
            existingConfig.set("notify-heart-drop", true);

        plugin.saveConfig();
    }
}