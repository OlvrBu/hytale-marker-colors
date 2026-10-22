package io.github.obut.markercolors;

import com.hypixel.hytale.math.vector.Transform;

import java.util.UUID;

/**
 * Test aid: a fake player that exists only as a map/compass marker, for testing colors without a second
 * player. Placed with {@code /markerdummy}; not saved.
 */
public final class DummyPlayer {

    public static final String NAME = "Dummy";
    /** Fixed, so colors chosen for the dummy survive moving it. */
    public static final UUID UUID = java.util.UUID.nameUUIDFromBytes("markercolors:dummy".getBytes());

    /** Where the dummy is, or null if not placed. Volatile: written by commands, read by the world map thread. */
    private volatile Placement placement;

    public record Placement(String worldName, Transform transform) {
    }

    public Placement get() {
        return placement;
    }

    public void place(String worldName, Transform transform) {
        placement = new Placement(worldName, transform);
    }

    public void remove() {
        placement = null;
    }
}
