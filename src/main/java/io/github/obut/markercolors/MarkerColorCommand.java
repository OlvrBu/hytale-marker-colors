package io.github.obut.markercolors;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.OptionalInt;
import java.util.UUID;

/** {@code /markercolor <player> <color|reset>}: sets how another player's marker looks to you. */
public class MarkerColorCommand extends AbstractPlayerCommand {

    // A plain string instead of ArgTypes.PLAYER_REF, so the test dummy (not a real player) can be a target too.
    private final RequiredArg<String> targetArg = withRequiredArg("player", "Online player whose marker to color", ArgTypes.STRING);
    private final RequiredArg<String> colorArg = withRequiredArg("color",
            "Color name (" + String.join(", ", MarkerColorParser.NAMED.keySet()) + "), #RRGGBB, or reset", ArgTypes.STRING);

    private final MarkerColorStore colors;

    public MarkerColorCommand(MarkerColorStore colors) {
        super("markercolor", "Choose the compass and map marker color of another player");
        // Every player may choose their own colors; the default group is "hytale:Adventurer" (like vanilla /emote).
        setPermissionGroups("hytale:Adventurer");
        this.colors = colors;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref,
                           @Nonnull PlayerRef playerRef, @Nonnull World world) {
        String targetInput = targetArg.get(context);
        UUID targetUuid;
        String targetName;
        if (targetInput.equalsIgnoreCase(DummyPlayer.NAME)) {
            targetUuid = DummyPlayer.UUID;
            targetName = DummyPlayer.NAME;
        } else {
            PlayerRef target = Universe.get().getPlayerByUsername(targetInput, NameMatching.DEFAULT);
            if (target == null) {
                context.sendMessage(Message.raw("No online player named '" + targetInput + "'."));
                return;
            }
            targetUuid = target.getUuid();
            targetName = target.getUsername();
        }
        String input = colorArg.get(context);
        if (input.equalsIgnoreCase("reset")) {
            colors.clear(playerRef.getUuid(), targetUuid);
            context.sendMessage(Message.raw(targetName + "'s marker is back to the default color."));
            return;
        }
        OptionalInt rgb = MarkerColorParser.parse(input);
        if (rgb.isEmpty()) {
            context.sendMessage(Message.raw("Unknown color '" + input + "'. Use one of "
                    + String.join(", ", MarkerColorParser.NAMED.keySet()) + ", a hex color like #00FF00, or reset."));
            return;
        }
        colors.set(playerRef.getUuid(), targetUuid, rgb.getAsInt());
        context.sendMessage(Message.raw(targetName + "'s marker is now " + MarkerColorParser.format(rgb.getAsInt()) + "."));
    }
}
