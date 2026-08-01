package org.xyplugin.xytitle.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.xyplugin.xytitle.XyTitlePlugin;
import org.xyplugin.xytitle.config.AttributeAmount;
import org.xyplugin.xytitle.config.GrowthDefinition;
import org.xyplugin.xytitle.config.TitleDefinition;
import org.xyplugin.xytitle.config.TitleRegistry;
import org.xyplugin.xytitle.data.OwnedTitle;
import org.xyplugin.xytitle.data.PlayerTitleData;
import org.xyplugin.xytitle.data.YamlTitleRepository;
import org.xyplugin.xytitle.integration.XyCoreBridge;
import org.xyplugin.xytitle.util.Durations;
import org.xyplugin.xytitle.util.Text;

public final class TitleService {

    private final XyTitlePlugin plugin;
    private final TitleRegistry registry;
    private final YamlTitleRepository repository;
    private final XyCoreBridge core;

    public TitleService(XyTitlePlugin plugin, TitleRegistry registry, YamlTitleRepository repository, XyCoreBridge core) {
        this.plugin = plugin;
        this.registry = registry;
        this.repository = repository;
        this.core = core;
    }

    public PlayerTitleData data(Player player) {
        return repository.get(player);
    }

    public boolean grant(Player player, String titleId, String durationOverride) {
        TitleDefinition title = registry.get(titleId);
        if (title == null) {
            return false;
        }
        long duration = Durations.parseToMillis(durationOverride);
        if (duration <= 0L) {
            duration = Durations.parseToMillis(title.duration());
        }
        long expiresAt = duration <= 0L ? 0L : System.currentTimeMillis() + duration;
        PlayerTitleData data = repository.get(player);
        data.addTitle(titleId, expiresAt);
        GrowthDefinition growth = registry.getGrowth(titleId);
        if (growth != null && data.growthLevel(titleId) <= 0) {
            data.growthLevel(titleId, growth.eligibleLevel(player.getLevel()));
        }
        saveIfNeeded(data);
        refresh(player);
        return true;
    }

    public boolean revoke(Player player, String titleId) {
        PlayerTitleData data = repository.get(player);
        if (!data.owns(titleId)) {
            return false;
        }
        data.removeTitle(titleId);
        saveIfNeeded(data);
        refresh(player);
        return true;
    }

    public void clear(Player player) {
        PlayerTitleData data = repository.get(player);
        List<String> ids = new ArrayList<String>(data.ownedIds());
        for (String id : ids) {
            data.removeTitle(id);
        }
        data.equippedTitle(null);
        saveIfNeeded(data);
        refresh(player);
    }

    public boolean equip(Player player, String titleId) {
        PlayerTitleData data = repository.get(player);
        if (!data.owns(titleId)) {
            return false;
        }
        data.equippedTitle(titleId);
        saveIfNeeded(data);
        refresh(player);
        return true;
    }

    public void unequip(Player player) {
        PlayerTitleData data = repository.get(player);
        data.equippedTitle(null);
        saveIfNeeded(data);
        refresh(player);
    }

    public void refresh(Player player) {
        PlayerTitleData data = repository.get(player);
        expireTitles(player, data);
        updateGrowth(player, data, false);
        updateDisplay(player, data);
        applyAttributes(player, data);
    }

    public void updateGrowth(Player player, PlayerTitleData data, boolean notify) {
        if (!registry.growthEnabled()) {
            return;
        }
        boolean changed = false;
        for (String titleId : new ArrayList<String>(data.ownedIds())) {
            GrowthDefinition growth = registry.getGrowth(titleId);
            if (growth == null) {
                continue;
            }
            int current = data.growthLevel(titleId);
            int eligible = growth.eligibleLevel(player.getLevel());
            if (eligible > current) {
                data.growthLevel(titleId, eligible);
                changed = true;
                if (notify) {
                    player.sendMessage(Text.color(core.getMessagePrefix() + "&a成长称号 &f"
                            + displayName(titleId) + " &a已提升至 &f" + eligible + "&a 级。"));
                }
            }
        }
        if (changed) {
            saveIfNeeded(data);
            applyAttributes(player, data);
        }
    }

    public Map<String, AttributeAmount> calculateAttributes(Player player) {
        return calculateAttributes(repository.get(player));
    }

    public Map<String, AttributeAmount> calculateAttributes(PlayerTitleData data) {
        Map<String, AttributeAmount> result = new HashMap<String, AttributeAmount>();
        String mode = plugin.getConfig().getString("settings.attribute-mode", "owned-all");
        if ("equipped-only".equalsIgnoreCase(mode)) {
            addTitleAttributes(result, data.equippedTitle(), data);
            return result;
        }
        for (String titleId : data.ownedIds()) {
            addTitleAttributes(result, titleId, data);
        }
        return result;
    }

    public List<String> attributeLines(Player player) {
        return attributeLines(calculateAttributes(player));
    }

    public List<String> attributeLines(Map<String, AttributeAmount> attributes) {
        List<String> lines = new ArrayList<String>();
        for (AttributeAmount amount : attributes.values()) {
            lines.add(amount.toAttributeLine());
        }
        return lines;
    }

    public String displayName(String titleId) {
        if (titleId == null || titleId.isEmpty()) {
            return plugin.getConfig().getString("display.no-title", "");
        }
        TitleDefinition title = registry.get(titleId);
        return title == null ? titleId : Text.color(title.displayName());
    }

    public String source(Player player) {
        return plugin.getConfig().getString("settings.attribute-source-prefix", "xytitle")
                + ":" + player.getUniqueId().toString();
    }

    public void unload(Player player) {
        core.clearAttributes(player, source(player));
        repository.unload(player, true);
    }

    public void saveAll() {
        repository.saveAll();
    }

    private void addTitleAttributes(Map<String, AttributeAmount> result, String titleId, PlayerTitleData data) {
        if (titleId == null) {
            return;
        }
        GrowthDefinition growth = registry.getGrowth(titleId);
        List<AttributeAmount> attributes;
        if (growth != null) {
            attributes = growth.attributesAt(data.growthLevel(titleId));
        } else {
            TitleDefinition title = registry.get(titleId);
            if (title == null) {
                return;
            }
            attributes = title.attributes();
        }
        for (AttributeAmount amount : attributes) {
            AttributeAmount previous = result.get(amount.key());
            result.put(amount.key(), previous == null ? amount : previous.add(amount));
        }
    }

    private void expireTitles(Player player, PlayerTitleData data) {
        boolean changed = false;
        Iterator<OwnedTitle> iterator = data.ownedTitles().iterator();
        while (iterator.hasNext()) {
            OwnedTitle owned = iterator.next();
            if (!owned.expired()) {
                continue;
            }
            String titleId = owned.titleId();
            iterator.remove();
            if (titleId.equals(data.equippedTitle())) {
                data.equippedTitle(null);
            }
            changed = true;
            player.sendMessage(Text.color(format("messages.expired", titleId, player)));
        }
        if (changed) {
            saveIfNeeded(data);
        }
    }

    private void updateDisplay(Player player, PlayerTitleData data) {
        String title = displayName(data.equippedTitle());
        if (plugin.getConfig().getBoolean("display.tab-prefix-enabled", true)) {
            String tab = plugin.getConfig().getString("display.tab-prefix", "&6{title} &f{player}")
                    .replace("{title}", title)
                    .replace("{player}", player.getName());
            player.setPlayerListName(Text.color(tab));
        }
        player.setDisplayName(Text.color(title.isEmpty() ? player.getName() : title + " " + player.getName()));
    }

    private void applyAttributes(Player player, PlayerTitleData data) {
        List<String> lines = attributeLines(calculateAttributes(data));
        boolean applied = core.applyAttributes(player, source(player), lines);
        if (!applied && core.attributesAvailable()) {
            plugin.getLogger().warning("XyCore refused XyTitle attribute source for " + player.getName());
        }
    }

    private void saveIfNeeded(PlayerTitleData data) {
        if (plugin.getConfig().getBoolean("settings.save-on-change", true)) {
            repository.save(data);
        }
    }

    private String message(String path) {
        return plugin.getConfig().getString(path, "");
    }

    private String format(String path, String titleId, Player player) {
        return core.getMessagePrefix() + message(path)
                .replace("{title}", displayName(titleId))
                .replace("{player}", player.getName());
    }
}
