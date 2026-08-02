package me.clip.placeholderapi.expansion;

import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI 2.x 最小编译契约。
 *
 * <p>该类只存在于编译用 stub sourceSet，不会被打包进 XyTitle jar。
 * 服务器运行时会链接 PlaceholderAPI 插件提供的正式类。</p>
 */
public abstract class PlaceholderExpansion {

    public abstract String getIdentifier();

    public abstract String getAuthor();

    public abstract String getVersion();

    public boolean persist() {
        return false;
    }

    public boolean canRegister() {
        return true;
    }

    public boolean register() {
        return false;
    }

    public boolean unregister() {
        return false;
    }

    public String onRequest(OfflinePlayer player, String params) {
        return null;
    }
}
