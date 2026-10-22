package io.github.obut.markercolors;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;

import javax.annotation.Nonnull;

/** Entry point. */
public class MarkerColorsPlugin extends JavaPlugin {

    private final MarkerColorStore colors = new MarkerColorStore();

    public MarkerColorsPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ColoredPlayersMarkerProvider provider = new ColoredPlayersMarkerProvider(colors);
        // Each world registers the vanilla provider in its WorldMapManager constructor; replace it under the
        // same key when the world is added (built-in plugins add their own providers on this event too).
        getEventRegistry().registerGlobal(AddWorldEvent.class, event -> {
            event.getWorld().getWorldMapManager().addMarkerProvider(ColoredPlayersMarkerProvider.KEY, provider);
            getLogger().atInfo().log("[MarkerColors] Replaced player marker provider in world %s", event.getWorld().getName());
        });
        getCommandRegistry().registerCommand(new MarkerColorCommand(colors));
    }
}
