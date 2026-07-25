package org.xyplugin.xytitle.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;

public final class Text {

    private Text() {
    }

    public static String color(String value) {
        if (value == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public static List<String> colorList(List<String> values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(color(value));
        }
        return result;
    }

    public static String plain(String value) {
        return ChatColor.stripColor(color(value == null ? "" : value));
    }
}
