package io.github.brainage04.brainagehud.waypoint;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;
import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/** Waypoint changes shared by the commands, the keys and the screens; each reports in chat. */
public final class WaypointActions {
    private WaypointActions() {}

    /** Creates a waypoint at the player's feet, named "Waypoint N" when {@code name} is null. */
    public static Optional<Waypoint> createAtPlayer(String name) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return Optional.empty();

        return create(name, player.blockPosition());
    }

    /** Creates a waypoint in the player's dimension, named "Waypoint N" when {@code name} is null. */
    public static Optional<Waypoint> create(String name, BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        Optional<List<Waypoint>> waypoints = WaypointStore.current();
        if (waypoints.isEmpty() || minecraft.level == null) {
            feedback(Component.literal("Waypoints can only be created in a world.").withStyle(ChatFormatting.RED));
            return Optional.empty();
        }

        List<Waypoint> list = waypoints.get();
        String waypointName = name == null ? WaypointStore.nextQuickName(list) : name.strip();
        if (waypointName.isEmpty()) {
            feedback(Component.literal("A waypoint needs a name.").withStyle(ChatFormatting.RED));
            return Optional.empty();
        }
        if (WaypointStore.find(list, waypointName).isPresent()) {
            feedback(Component.literal("A waypoint called \"%s\" already exists.".formatted(waypointName)).withStyle(ChatFormatting.RED));
            return Optional.empty();
        }

        Waypoint waypoint = new Waypoint(
                waypointName,
                pos,
                minecraft.level.dimension().identifier().toString(),
                WaypointStore.nextColour(list));
        list.add(waypoint);
        WaypointStore.save();

        MutableComponent message = Component.literal("Created waypoint ")
                .append(name(waypoint))
                .append(" at %s.".formatted(pos.toShortString()));
        if (!getConfig().waypointConfig.showInWorld) {
            message.append(Component.literal(" Turn on Show In World in the BrainageHUD waypoint config to see it.").withStyle(ChatFormatting.GRAY));
        }
        feedback(message);

        return Optional.of(waypoint);
    }

    public static boolean remove(String name) {
        Optional<List<Waypoint>> waypoints = WaypointStore.current();
        Optional<Waypoint> waypoint = waypoints.flatMap(list -> WaypointStore.find(list, name));
        if (waypoint.isEmpty()) {
            feedback(Component.literal("There is no waypoint called \"%s\".".formatted(name)).withStyle(ChatFormatting.RED));
            return false;
        }

        waypoints.get().remove(waypoint.get());
        WaypointStore.save();
        feedback(Component.literal("Removed waypoint ").append(name(waypoint.get())).append("."));
        return true;
    }

    /** The waypoint's name in its colour. */
    public static MutableComponent name(Waypoint waypoint) {
        return Component.literal(waypoint.name).withStyle(style -> style.withColor(TextColor.fromRgb(waypoint.colour)));
    }
}
