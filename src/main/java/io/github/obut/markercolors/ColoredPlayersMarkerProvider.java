package io.github.obut.markercolors;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.worldmap.HeightDeltaIconComponent;
import com.hypixel.hytale.protocol.packets.worldmap.PlayerMarkerComponent;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.PlayersMapMarkerConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.HiddenPlayersManager;
import com.hypixel.hytale.server.core.modules.entity.component.Spectating;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * Replaces the vanilla {@code OtherPlayersMarkerProvider} (key "playerIcons"). Same logic as vanilla 0.7.2,
 * plus a {@link TintComponent} when the viewer chose a color for that player. The server calls
 * {@code update} once per viewing player, so each viewer gets their own colors. The marker feeds both the
 * world map and the compass.
 * <p>
 * Re-sync with vanilla {@code OtherPlayersMarkerProvider} after game updates.
 */
public class ColoredPlayersMarkerProvider implements WorldMapManager.MarkerProvider {

    /** Key of the vanilla player marker provider in {@code WorldMapManager}; registering under it replaces vanilla. */
    public static final String KEY = "playerIcons";

    private final MarkerColorStore colors;

    public ColoredPlayersMarkerProvider(MarkerColorStore colors) {
        this.colors = colors;
    }

    @Override
    public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
        WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
        if (!worldMapConfig.isDisplayPlayers()) {
            return;
        }
        PlayersMapMarkerConfig playersMapConfig = worldMapConfig.getPlayersConfig();
        Predicate<PlayerRef> playerMapFilter = collector.getPlayerMapFilter();
        // The MarkerProvider interface hands us the deprecated Player; switch to its PlayerRef right away.
        PlayerRef viewer = player.getPlayerRef();
        HiddenPlayersManager hiddenPlayersManager = viewer.getHiddenPlayersManager();
        ComponentType<EntityStore, Spectating> spectatingComponentType = Spectating.getComponentType();
        boolean viewerSpectating = viewer.getComponentConcurrent(spectatingComponentType) != null;

        for (PlayerRef otherPlayer : world.getPlayerRefs()) {
            if (otherPlayer.getUuid().equals(viewer.getUuid())
                    || (!viewerSpectating && otherPlayer.getComponentConcurrent(spectatingComponentType) != null)
                    || hiddenPlayersManager.isPlayerHidden(otherPlayer.getUuid())
                    || (playerMapFilter != null && playerMapFilter.test(otherPlayer))) {
                continue;
            }
            MapMarkerBuilder builder = new MapMarkerBuilder("Player-" + otherPlayer.getUuid(), "Player.png", otherPlayer.getTransform())
                    .withCustomName(otherPlayer.getUsername())
                    .withComponent(new PlayerMarkerComponent(otherPlayer.getUuid()))
                    .withComponent(new HeightDeltaIconComponent(
                            playersMapConfig.getIconSwapHeightDelta(),
                            playersMapConfig.getAboveIcon(),
                            playersMapConfig.getIconSwapHeightDelta(),
                            playersMapConfig.getBelowIcon()));
            Integer rgb = colors.get(viewer.getUuid(), otherPlayer.getUuid());
            if (rgb != null) {
                builder.withComponent(new TintComponent(toProtocolColor(rgb)));
            }
            collector.addIgnoreViewDistance(builder.build());
        }
    }

    private static Color toProtocolColor(int rgb) {
        return new Color((byte) (rgb >> 16), (byte) (rgb >> 8), (byte) rgb);
    }
}
