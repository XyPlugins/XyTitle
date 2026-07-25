package org.xyplugin.xytitle.placeholder;

import java.util.Map;
import org.bukkit.entity.Player;
import org.xyplugin.xycore.api.placeholder.PlaceholderProvider;
import org.xyplugin.xytitle.config.AttributeAmount;
import org.xyplugin.xytitle.data.PlayerTitleData;
import org.xyplugin.xytitle.service.TitleService;

public final class TitlePlaceholderProvider implements PlaceholderProvider {

    private final TitleService service;

    public TitlePlaceholderProvider(TitleService service) {
        this.service = service;
    }

    @Override
    public String getNamespace() {
        return "xytitle";
    }

    @Override
    public String resolve(Player player, String params) {
        if (player == null) {
            return "";
        }
        String key = params == null ? "title" : params.toLowerCase();
        PlayerTitleData data = service.data(player);
        if ("title".equals(key) || "current_title".equals(key) || "display".equals(key)) {
            return service.displayName(data.equippedTitle());
        }
        if ("title_id".equals(key) || "current_title_id".equals(key)) {
            return data.equippedTitle() == null ? "" : data.equippedTitle();
        }
        if ("has_title".equals(key)) {
            return data.equippedTitle() == null ? "false" : "true";
        }
        if ("owned_count".equals(key)) {
            return String.valueOf(data.ownedIds().size());
        }
        if ("attributes".equals(key)) {
            StringBuilder builder = new StringBuilder();
            for (AttributeAmount amount : service.calculateAttributes(player).values()) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(amount.toAttributeLine());
            }
            return builder.toString();
        }
        if (key.startsWith("has_")) {
            return data.owns(params.substring(4)) ? "true" : "false";
        }
        if (key.startsWith("attr_")) {
            String attributeName = params.substring(5);
            for (Map.Entry<String, AttributeAmount> entry : service.calculateAttributes(player).entrySet()) {
                AttributeAmount amount = entry.getValue();
                if (amount.name().equalsIgnoreCase(attributeName)) {
                    return amount.toAttributeLine();
                }
            }
        }
        return "";
    }
}
