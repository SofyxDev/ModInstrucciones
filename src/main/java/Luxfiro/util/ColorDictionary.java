package Luxfiro.instrucciones.util;

import java.util.HashMap;
import java.util.Map;

public class ColorDictionary {
    public static final Map<String, Integer> COLOR_MAP = new HashMap<>();

    static {
        Map<String, String> defaultColors = Map.ofEntries(
                Map.entry("white", "#FFFFFF"), Map.entry("red", "#FF5555"), Map.entry("green", "#55FF55"),
                Map.entry("blue", "#5555FF"), Map.entry("gold", "#FFAA00"), // ... (Resto de tu mapa de colores aquí)
                Map.entry("light_purple", "#FF55FF")
        );
        defaultColors.forEach((k, v) -> COLOR_MAP.put(k, Integer.parseInt(v.replace("#", ""), 16) | 0xFF000000));
    }

    public static int parseColor(String input, int fallback) {
        if (input.startsWith("#")) {
            try { return (int) Long.parseLong(input.replace("#", ""), 16) | 0xFF000000; }
            catch (Exception e) { return fallback; }
        }
        return COLOR_MAP.getOrDefault(input.toLowerCase(), fallback);
    }
}