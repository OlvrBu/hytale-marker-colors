package io.github.obut.markercolors;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.UUID;

/**
 * The saved-colors file format, game-independent (Gson ships inside the server jar):
 * <pre>{ "viewer-uuid": { "target-uuid": "#RRGGBB", ... }, ... }</pre>
 */
public final class MarkerColorJson {

    /** Parsed colors plus how many entries were skipped because they were malformed. */
    public record Result(Map<UUID, Map<UUID, Integer>> colors, int skipped) {
    }

    private MarkerColorJson() {
    }

    /** @throws IllegalArgumentException if the text isn't a JSON object */
    public static Result parse(String json) {
        JsonElement root;
        try {
            root = JsonParser.parseString(json);
        } catch (RuntimeException e) { // Gson's JsonParseException is unchecked
            throw new IllegalArgumentException("Not valid JSON: " + e.getMessage(), e);
        }
        if (!root.isJsonObject()) {
            throw new IllegalArgumentException("Expected a JSON object at the top level");
        }
        Map<UUID, Map<UUID, Integer>> colors = new HashMap<>();
        int skipped = 0;
        for (Map.Entry<String, JsonElement> viewerEntry : root.getAsJsonObject().entrySet()) {
            UUID viewer = parseUuid(viewerEntry.getKey());
            if (viewer == null || !viewerEntry.getValue().isJsonObject()) {
                skipped++;
                continue;
            }
            Map<UUID, Integer> forViewer = new HashMap<>();
            for (Map.Entry<String, JsonElement> targetEntry : viewerEntry.getValue().getAsJsonObject().entrySet()) {
                UUID target = parseUuid(targetEntry.getKey());
                OptionalInt rgb = parseHex(targetEntry.getValue());
                if (target == null || rgb.isEmpty()) {
                    skipped++;
                    continue;
                }
                forViewer.put(target, rgb.getAsInt());
            }
            if (!forViewer.isEmpty()) {
                colors.put(viewer, forViewer);
            }
        }
        return new Result(colors, skipped);
    }

    public static String write(Map<UUID, Map<UUID, Integer>> colors) {
        JsonObject root = new JsonObject();
        // Sorted so the file doesn't reshuffle on every save.
        for (Map.Entry<UUID, Map<UUID, Integer>> viewerEntry : new TreeMap<>(colors).entrySet()) {
            if (viewerEntry.getValue().isEmpty()) {
                continue;
            }
            JsonObject forViewer = new JsonObject();
            for (Map.Entry<UUID, Integer> targetEntry : new TreeMap<>(viewerEntry.getValue()).entrySet()) {
                forViewer.addProperty(targetEntry.getKey().toString(), MarkerColorParser.format(targetEntry.getValue()));
            }
            root.add(viewerEntry.getKey().toString(), forViewer);
        }
        return new GsonBuilder().setPrettyPrinting().create().toJson(root) + "\n";
    }

    private static UUID parseUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Only hex here, not color names, so renaming a palette entry can't break saved files. */
    private static OptionalInt parseHex(JsonElement value) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return OptionalInt.empty();
        }
        String s = value.getAsString().trim();
        return s.startsWith("#") ? MarkerColorParser.parse(s) : OptionalInt.empty();
    }
}
