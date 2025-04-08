package me.evade.LifeSteel;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class BannedPlayersManager {
    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, String> bannedPlayers;
    public BannedPlayersManager(JavaPlugin plugin, File dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
        this.bannedPlayers = new HashMap<>();
        loadData();
    }
    public void loadData() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml file!");
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        if (dataConfig.contains("banned-players")) {
            for (String uuidString : dataConfig.getConfigurationSection("banned-players").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                bannedPlayers.put(uuid, "");
            }
        }
    }
    public void saveData() {
        if (dataConfig == null) {
            return;
        }
        dataConfig.set("banned-players", null);
        for (UUID uuid : bannedPlayers.keySet()) {
            dataConfig.set("banned-players." + uuid.toString(), true);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml file!");
            e.printStackTrace();
        }
    }
    public void banPlayer(UUID uuid, String playerName) {
        bannedPlayers.put(uuid, "");
        saveData();
    }
    public void revivePlayer(UUID uuid) {
        bannedPlayers.remove(uuid);
        saveData();
    }
    public boolean isPlayerBanned(UUID uuid) {
        return bannedPlayers.containsKey(uuid);
    }
    public UUID getUUIDFromName(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player != null && isPlayerBanned(player.getUniqueId())) {
            return player.getUniqueId();
        }
        for (UUID uuid : bannedPlayers.keySet()) {
            String offlinePlayerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (offlinePlayerName != null && offlinePlayerName.equalsIgnoreCase(playerName)) {
                return uuid;
            }
        }
        return null;
    }
}