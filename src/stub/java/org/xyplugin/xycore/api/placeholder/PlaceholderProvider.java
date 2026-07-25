package org.xyplugin.xycore.api.placeholder;

import org.bukkit.entity.Player;

public interface PlaceholderProvider {
    String getNamespace();

    String resolve(Player player, String params);
}
