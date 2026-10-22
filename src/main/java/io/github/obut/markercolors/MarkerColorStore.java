package io.github.obut.markercolors;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which color each viewer chose for which other player (viewer UUID -> target UUID -> 0xRRGGBB).
 * In memory only for now, so choices are lost on server restart. Concurrent because the world map
 * thread reads it while commands write it.
 */
public final class MarkerColorStore {

    private final Map<UUID, Map<UUID, Integer>> colors = new ConcurrentHashMap<>();

    /** The color {@code viewer} chose for {@code target}, or null for the default marker. */
    public Integer get(UUID viewer, UUID target) {
        Map<UUID, Integer> forViewer = colors.get(viewer);
        return forViewer == null ? null : forViewer.get(target);
    }

    public void set(UUID viewer, UUID target, int rgb) {
        colors.computeIfAbsent(viewer, k -> new ConcurrentHashMap<>()).put(target, rgb);
    }

    public void clear(UUID viewer, UUID target) {
        Map<UUID, Integer> forViewer = colors.get(viewer);
        if (forViewer != null) {
            forViewer.remove(target);
        }
    }
}
