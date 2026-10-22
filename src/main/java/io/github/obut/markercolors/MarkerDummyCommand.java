package io.github.obut.markercolors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/**
 * {@code /markerdummy}: test aid (operator-only). Places the dummy player's marker at your position, or removes
 * it if you run it again while standing within 2 blocks of it.
 */
public class MarkerDummyCommand extends AbstractPlayerCommand {

    private final DummyPlayer dummy;

    public MarkerDummyCommand(DummyPlayer dummy) {
        super("markerdummy", "Place a fake player marker at your position (run again next to it to remove)");
        this.dummy = dummy;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef, @Nonnull World world) {
        // Copy: getTransform() is the player's live transform, which keeps changing as they move.
        Transform here = new Transform(playerRef.getTransform());
        DummyPlayer.Placement current = dummy.get();
        if (current != null && current.worldName().equals(world.getName())
                && current.transform().getPosition().distance(here.getPosition()) < 2.0) {
            dummy.remove();
            context.sendMessage(Message.raw("Removed the dummy player marker."));
            return;
        }
        dummy.place(world.getName(), here);
        context.sendMessage(Message.raw("Placed the dummy player marker here. Color it with /markercolor "
                + DummyPlayer.NAME + " <color>; run /markerdummy again next to it to remove it."));
    }
}
