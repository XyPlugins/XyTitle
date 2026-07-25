package org.xyplugin.xytitle.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerTitleData {

    private final UUID playerId;
    private String playerName;
    private final Map<String, OwnedTitle> ownedTitles = new HashMap<String, OwnedTitle>();
    private final Map<String, Integer> growthLevels = new HashMap<String, Integer>();
    private String equippedTitle;

    public PlayerTitleData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public UUID playerId() {
        return playerId;
    }

    public String playerName() {
        return playerName;
    }

    public void playerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean owns(String titleId) {
        return ownedTitles.containsKey(titleId);
    }

    public void addTitle(String titleId, long expiresAtMillis) {
        ownedTitles.put(titleId, new OwnedTitle(titleId, expiresAtMillis));
    }

    public void removeTitle(String titleId) {
        ownedTitles.remove(titleId);
        growthLevels.remove(titleId);
        if (titleId != null && titleId.equals(equippedTitle)) {
            equippedTitle = null;
        }
    }

    public Collection<OwnedTitle> ownedTitles() {
        return ownedTitles.values();
    }

    public Set<String> ownedIds() {
        return ownedTitles.keySet();
    }

    public String equippedTitle() {
        return equippedTitle;
    }

    public void equippedTitle(String equippedTitle) {
        this.equippedTitle = equippedTitle;
    }

    public int growthLevel(String titleId) {
        Integer level = growthLevels.get(titleId);
        return level == null ? 0 : level;
    }

    public void growthLevel(String titleId, int level) {
        growthLevels.put(titleId, level);
    }

    public Map<String, Integer> growthLevels() {
        return growthLevels;
    }

    public boolean empty() {
        return ownedTitles.isEmpty() && equippedTitle == null;
    }
}
