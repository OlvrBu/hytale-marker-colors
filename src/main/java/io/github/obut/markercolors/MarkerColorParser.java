package io.github.obut.markercolors;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalInt;

/** Parses a color given by name ("green") or hex ("#00ff00", "00FF00") into 0xRRGGBB. Game-independent. */
public final class MarkerColorParser {

    /**
     * Named colors offered to players, in menu order. Same palette as the vanilla placeable map markers (0.7.2):
     * the client doesn't send its palette, so the values were read from markers placed in each color.
     */
    public static final Map<String, Integer> NAMED = new LinkedHashMap<>();

    static {
        NAMED.put("white", 0xFFFFFF);
        NAMED.put("pink", 0xFF1399);
        NAMED.put("orange", 0xFF7512);
        NAMED.put("yellow", 0xECE133);
        NAMED.put("green", 0x00F482);
        NAMED.put("blue", 0x1880FF);
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
