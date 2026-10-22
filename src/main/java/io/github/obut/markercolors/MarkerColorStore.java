package io.github.obut.markercolors;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Which color each viewer chose for which other player (viewer UUID -> target UUID -> 0xRRGGBB).
 * Concurrent because the world map thread reads it while commands write it. Saving is done by whoever
 * listens via {@link #setOnChange}.
 */
public final class MarkerColorStore {

    private final Map<UUID, Map<UUID, Integer>> colors = new ConcurrentHashMap<>();
    private volatile Runnable onChange = () -> {
    };

    /** Called after every {@link #set} or {@link #clear}; not after {@link #replaceAll}. */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange;
    }

    /** The color {@code viewer} chose for {@code target}, or null for the default marker. */
    public Integer get(UUID viewer, UUID target) {
        Map<UUID, Integer> forViewer = colors.get(viewer);
        return forViewer == null ? null : forViewer.get(target);
    }

    public void set(UUID viewer, UUID target, int rgb) {
        colors.computeIfAbsent(viewer, k -> new ConcurrentHashMap<>()).put(target, rgb);
        onChange.run();
    }

    public void clear(UUID viewer, UUID target) {
        Map<UUID, Integer> forViewer = colors.get(viewer);
        if (forViewer != null && forViewer.remove(target) != null) {
            onChange.run();
        }
    }

    /** Replaces all colors, e.g. with the ones loaded from disk. */
    public void replaceAll(Map<UUID, Map<UUID, Integer>> loaded) {
        colors.clear();
        loaded.forEach((viewer, forViewer) -> colors.put(viewer, new ConcurrentHashMap<>(forViewer)));
    }

    /** A copy of all colors, safe to iterate while others keep changing the store. */
    public Map<UUID, Map<UUID, Integer>> snapshot() {
        Map<UUID, Map<UUID, Integer>> copy = new HashMap<>();
        colors.forEach((viewer, forViewer) -> copy.put(viewer, new HashMap<>(forViewer)));
        return copy;
    }
}
