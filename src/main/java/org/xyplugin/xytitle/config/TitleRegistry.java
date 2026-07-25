package org.xyplugin.xytitle.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TitleRegistry {

    private final JavaPlugin plugin;
    private final Map<String, TitleDefinition> titles = new HashMap<String, TitleDefinition>();
    private final Map<String, GrowthDefinition> growthTitles = new HashMap<String, GrowthDefinition>();
    private boolean growthEnabled;

    public TitleRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        titles.clear();
        growthTitles.clear();
        plugin.reloadConfig();
        loadRegularTitles();
        loadGrowthTitles();
    }

    private void loadRegularTitles() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("titles");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection titleSection = section.getConfigurationSection(id);
            if (titleSection == null) {
                continue;
            }
            titles.put(id, readTitle(id, titleSection, false));
        }
    }

    private void loadGrowthTitles() {
        File file = new File(plugin.getDataFolder(), "growth_titles.yml");
        if (!file.isFile()) {
            plugin.saveResource("growth_titles.yml", false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        growthEnabled = yaml.getBoolean("settings.enabled", true);
        if (!growthEnabled) {
            return;
        }
        ConfigurationSection root = yaml.getConfigurationSection("growth-titles");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            TitleDefinition base = readTitle(id, section, true);
            Map<Integer, GrowthDefinition.GrowthLevel> levels = new HashMap<Integer, GrowthDefinition.GrowthLevel>();
            ConfigurationSection levelRoot = section.getConfigurationSection("levels");
            if (levelRoot != null) {
                for (String rawLevel : levelRoot.getKeys(false)) {
                    ConfigurationSection levelSection = levelRoot.getConfigurationSection(rawLevel);
                    if (levelSection == null) {
                        continue;
                    }
                    int level = parseInt(rawLevel, levelSection.getInt("level", 0));
                    if (level <= 0) {
                        continue;
                    }
                    levels.put(level, new GrowthDefinition.GrowthLevel(
                            level,
                            levelSection.getInt("required-level", 0),
                            readAttributes(levelSection.getConfigurationSection("attributes"))));
                }
            }
            GrowthDefinition growth = new GrowthDefinition(base, levels);
            growthTitles.put(id, growth);
            titles.put(id, base);
        }
    }

    private TitleDefinition readTitle(String id, ConfigurationSection section, boolean growth) {
        String displayName = section.getString("display-name", section.getString("display_name", id));
        Material material = Material.matchMaterial(section.getString("item-material", section.getString("item_material", "PAPER")));
        List<String> lore = section.getStringList("lore");
        List<AttributeAmount> attributes = readAttributes(section.getConfigurationSection(growth ? "base-attributes" : "attributes"));
        String duration = section.getString("duration", section.getString("time"));
        return new TitleDefinition(id, displayName, material == null ? Material.PAPER : material, lore, attributes, duration, growth);
    }

    private List<AttributeAmount> readAttributes(ConfigurationSection section) {
        List<AttributeAmount> attributes = new ArrayList<AttributeAmount>();
        if (section == null) {
            return attributes;
        }
        for (String name : section.getKeys(false)) {
            Object raw = section.get(name);
            AttributeAmount amount = parseAttribute(name, raw);
            if (amount != null) {
                attributes.add(amount);
            }
        }
        return attributes;
    }

    private AttributeAmount parseAttribute(String name, Object raw) {
        if (raw == null) {
            return null;
        }
        boolean percentage = false;
        double value;
        if (raw instanceof Number) {
            value = ((Number) raw).doubleValue();
        } else {
            String text = String.valueOf(raw).trim().replace("+", "");
            if (text.endsWith("%")) {
                percentage = true;
                text = text.substring(0, text.length() - 1);
            }
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return new AttributeAmount(name, value, percentage);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public TitleDefinition get(String id) {
        return titles.get(id);
    }

    public GrowthDefinition getGrowth(String id) {
        return growthTitles.get(id);
    }

    public boolean exists(String id) {
        return titles.containsKey(id);
    }

    public Collection<TitleDefinition> all() {
        return Collections.unmodifiableCollection(titles.values());
    }

    public List<String> ids() {
        List<String> ids = new ArrayList<String>(titles.keySet());
        Collections.sort(ids);
        return ids;
    }

    public boolean growthEnabled() {
        return growthEnabled;
    }
}
