package org.xyplugin.xytitle.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public final class GrowthDefinition {

    private final TitleDefinition title;
    private final Map<Integer, GrowthLevel> levels;

    public GrowthDefinition(TitleDefinition title, Map<Integer, GrowthLevel> levels) {
        this.title = title;
        this.levels = new HashMap<Integer, GrowthLevel>(levels);
    }

    public TitleDefinition title() {
        return title;
    }

    public int maxLevel() {
        int max = 0;
        for (Integer level : levels.keySet()) {
            max = Math.max(max, level);
        }
        return max;
    }

    public GrowthLevel level(int level) {
        return levels.get(level);
    }

    public int eligibleLevel(int playerLevel) {
        int result = 0;
        for (GrowthLevel level : levels.values()) {
            if (playerLevel >= level.requiredPlayerLevel()) {
                result = Math.max(result, level.level());
            }
        }
        return result;
    }

    public List<AttributeAmount> attributesAt(int currentLevel) {
        List<AttributeAmount> result = new ArrayList<AttributeAmount>(title.attributes());
        GrowthLevel level = levels.get(currentLevel);
        if (level != null) {
            result.addAll(level.attributes());
        }
        return result;
    }

    public static GrowthDefinition empty(String id) {
        return new GrowthDefinition(new TitleDefinition(id, id, Material.PAPER,
                new ArrayList<String>(), new ArrayList<AttributeAmount>(), null, true),
                new HashMap<Integer, GrowthLevel>());
    }

    public static final class GrowthLevel {
        private final int level;
        private final int requiredPlayerLevel;
        private final List<AttributeAmount> attributes;

        public GrowthLevel(int level, int requiredPlayerLevel, List<AttributeAmount> attributes) {
            this.level = level;
            this.requiredPlayerLevel = requiredPlayerLevel;
            this.attributes = new ArrayList<AttributeAmount>(attributes);
        }

        public int level() {
            return level;
        }

        public int requiredPlayerLevel() {
            return requiredPlayerLevel;
        }

        public List<AttributeAmount> attributes() {
            return new ArrayList<AttributeAmount>(attributes);
        }
    }
}
