package org.xyplugin.xytitle.data;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlTitleRepository {

    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerTitleData> cache = new HashMap<UUID, PlayerTitleData>();

    public YamlTitleRepository(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
    }

    public PlayerTitleData get(Player player) {
        PlayerTitleData data = cache.get(player.getUniqueId());
        if (data == null) {
            data = load(player.getUniqueId(), player.getName());
            cache.put(player.getUniqueId(), data);
        } else {
            data.playerName(player.getName());
        }
        return data;
    }

    public PlayerTitleData load(UUID uuid, String playerName) {
        PlayerTitleData data = new PlayerTitleData(uuid, playerName);
        File file = file(uuid);
        if (!file.isFile()) {
            return data;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.equippedTitle(yaml.getString("equipped-title", yaml.getString("current_title")));

        ConfigurationSection owned = yaml.getConfigurationSection("owned");
        if (owned != null) {
            for (String id : owned.getKeys(false)) {
                long expiresAt = owned.getLong(id + ".expires-at", 0L);
                data.addTitle(id, expiresAt);
            }
        } else {
            for (String id : yaml.getStringList("owned_titles")) {
                data.addTitle(id, 0L);
            }
        }

        ConfigurationSection growth = yaml.getConfigurationSection("growth-levels");
        if (growth != null) {
            for (String id : growth.getKeys(false)) {
                data.growthLevel(id, growth.getInt(id, 0));
            }
        }
        return data;
    }

    public void save(PlayerTitleData data) {
        if (!folder.isDirectory() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create playerdata folder.");
            return;
        }
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("player-name", data.playerName());
        yaml.set("equipped-title", data.equippedTitle());
        for (OwnedTitle ownedTitle : data.ownedTitles()) {
            yaml.set("owned." + ownedTitle.titleId() + ".expires-at", ownedTitle.expiresAtMillis());
        }
        for (Map.Entry<String, Integer> entry : data.growthLevels().entrySet()) {
            yaml.set("growth-levels." + entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(file(data.playerId()));
        } catch (IOException exception) {
            plugin.getLogger().warning("Could not save title data for " + data.playerName() + ": " + exception.getMessage());
        }
    }

    public void unload(Player player, boolean save) {
        PlayerTitleData data = cache.remove(player.getUniqueId());
        if (save && data != null) {
            save(data);
        }
    }

    public void saveAll() {
        for (PlayerTitleData data : cache.values()) {
            save(data);
        }
    }

    private File file(UUID uuid) {
        return new File(folder, uuid.toString() + ".yml");
    }
}
