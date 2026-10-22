package io.github.obut.markercolors;

import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Saves the chosen colors to {@code marker_colors.json} in the plugin's data folder, so they survive restarts.
 * Written in full after every change; the file is tiny.
 */
public final class MarkerColorFile {

    public static final String FILE_NAME = "marker_colors.json";

    private final Path file;
    private final HytaleLogger logger;

    public MarkerColorFile(Path dataDirectory, HytaleLogger logger) {
        this.file = dataDirectory.resolve(FILE_NAME);
        this.logger = logger;
    }

    /**
     * Loads saved colors into {@code store}. A missing file means no colors yet. An unreadable file is moved
     * aside (so the next save can't overwrite it) and the plugin starts with no colors; it never stops the server.
     */
    public void load(MarkerColorStore store) {
        if (!Files.exists(file)) {
            logger.at(Level.INFO).log("[MarkerColors] No %s yet, starting without saved colors", FILE_NAME);
            return;
        }
        try {
            MarkerColorJson.Result result = MarkerColorJson.parse(Files.readString(file, StandardCharsets.UTF_8));
            store.replaceAll(result.colors());
            if (result.skipped() > 0) {
                logger.at(Level.WARNING).log("[MarkerColors] Skipped %d malformed entries in %s", result.skipped(), file);
            }
            logger.at(Level.INFO).log("[MarkerColors] Loaded colors of %d player(s) from %s", result.colors().size(), file);
        } catch (IOException | IllegalArgumentException e) {
            Path aside = file.resolveSibling(FILE_NAME + ".broken-" + System.currentTimeMillis());
            logger.at(Level.SEVERE).log("[MarkerColors] Could not read %s (%s); moved it to %s and starting without saved colors",
                    file, e.getMessage(), aside.getFileName());
            try {
                Files.move(file, aside);
            } catch (IOException moveError) {
                logger.at(Level.SEVERE).log("[MarkerColors] Could not move %s aside: %s", file, moveError.getMessage());
            }
        }
    }

    /**
     * Writes all colors, via a temporary file so a crash can't leave half a file. Synchronized because commands
     * from different worlds run on different threads. Failures are logged, not thrown: the in-memory colors
     * still work, they just won't survive a restart.
     */
    public synchronized void save(MarkerColorStore store) {
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(FILE_NAME + ".tmp");
            Files.writeString(temp, MarkerColorJson.write(store.snapshot()), StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.at(Level.SEVERE).log("[MarkerColors] Could not save %s: %s", file, e.getMessage());
        }
    }
}
