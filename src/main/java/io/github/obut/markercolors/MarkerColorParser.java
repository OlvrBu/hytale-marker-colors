package io.github.obut.markercolors;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

/** Parses a color given by name ("green") or hex ("#00ff00", "00FF00") into 0xRRGGBB. Game-independent. */
public final class MarkerColorParser {

    /** Named colors offered to players, in the order they're listed in help text. */
    public static final Map<String, Integer> NAMED = new LinkedHashMap<>();

    static {
        NAMED.put("red", 0xFF4040);
        NAMED.put("orange", 0xFF9020);
        NAMED.put("yellow", 0xFFE030);
        NAMED.put("green", 0x40E040);
        NAMED.put("cyan", 0x40E0E0);
        NAMED.put("blue", 0x4070FF);
        NAMED.put("purple", 0xB050FF);
        NAMED.put("pink", 0xFF70C0);
        NAMED.put("white", 0xFFFFFF);
        NAMED.put("black", 0x202020);
    }

    private MarkerColorParser() {
    }

    public static OptionalInt parse(String input) {
        String s = input.trim().toLowerCase(Locale.ROOT);
        Integer named = NAMED.get(s);
        if (named != null) {
            return OptionalInt.of(named);
        }
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        if (!s.matches("[0-9a-f]{6}")) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(s, 16));
    }

    public static String format(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }
}
