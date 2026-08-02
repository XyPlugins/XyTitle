package org.xyplugin.xytitle.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.xyplugin.xytitle.XyTitlePlugin;

public final class TitlePapiExpansion extends PlaceholderExpansion {

    private final XyTitlePlugin plugin;
    private final TitlePlaceholderProvider provider;

    public TitlePapiExpansion(XyTitlePlugin plugin, TitlePlaceholderProvider provider) {
        this.plugin = plugin;
        this.provider = provider;
    }

    @Override
    public String getIdentifier() {
        return provider.getNamespace();
    }

    @Override
    public String getAuthor() {
        return "XyPlugin";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        Player player = offlinePlayer == null ? null : offlinePlayer.getPlayer();
        return provider.resolve(player, params == null ? "" : params);
    }
}
