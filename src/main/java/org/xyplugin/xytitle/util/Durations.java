package org.xyplugin.xytitle.util;

import java.util.Locale;

public final class Durations {

    private Durations() {
    }

    public static long parseToMillis(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0L;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        try {
            long multiplier = 86400000L;
            if (normalized.endsWith("d")) {
                normalized = normalized.substring(0, normalized.length() - 1);
                multiplier = 86400000L;
            } else if (normalized.endsWith("h")) {
                normalized = normalized.substring(0, normalized.length() - 1);
                multiplier = 3600000L;
            } else if (normalized.endsWith("m")) {
                normalized = normalized.substring(0, normalized.length() - 1);
                multiplier = 60000L;
            } else if (normalized.endsWith("s")) {
                normalized = normalized.substring(0, normalized.length() - 1);
                multiplier = 1000L;
            }
            long amount = Long.parseLong(normalized);
            return Math.max(0L, amount * multiplier);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    public static String formatRemaining(long expiresAtMillis) {
        if (expiresAtMillis <= 0L) {
            return "永久";
        }
        long remaining = Math.max(0L, expiresAtMillis - System.currentTimeMillis()) / 1000L;
        long days = remaining / 86400L;
        long hours = (remaining % 86400L) / 3600L;
        long minutes = (remaining % 3600L) / 60L;
        if (days > 0L) {
            return days + "天" + hours + "小时";
        }
        if (hours > 0L) {
            return hours + "小时" + minutes + "分钟";
        }
        return Math.max(1L, minutes) + "分钟";
    }
}
