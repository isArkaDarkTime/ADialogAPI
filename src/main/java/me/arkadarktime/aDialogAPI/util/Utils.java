package me.arkadarktime.aDialogAPI.util;

import java.util.Map;

public class Utils {
    public static String resolveString(Map<?, ?> entry, String... keys) {
        for (String key : keys) {
            Object val = entry.get(key);
            if (val != null && !val.toString().isBlank()) {
                return val.toString();
            }
        }
        return null;
    }

    public static int parseIntSafe(Object obj, int fallback) {
        if (obj == null) return fallback;
        try {
            return Integer.parseInt(obj.toString());
        } catch ( NumberFormatException e ) {
            return fallback;
        }
    }

    public static float parseFloatSafe(Object obj, float fallback) {
        if (obj == null) return fallback;
        try {
            return Float.parseFloat(obj.toString());
        } catch ( NumberFormatException e ) {
            return fallback;
        }
    }

    public static boolean parseBoolSafe(Object obj, boolean fallback) {
        if (obj == null) return fallback;
        return Boolean.parseBoolean(obj.toString());
    }
}
