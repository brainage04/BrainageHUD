package io.github.brainage04.brainagehud.waypoint;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.util.ChatFeedback;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

/** Waypoint changes shared by the commands, the keys and the screens; each reports in chat. */
public final class WaypointActions {
    public static final int MAX_NAME_LENGTH = 64;

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
            ChatFeedback.error("Waypoints can only be created in a world.");
            return Optional.empty();
        }

        List<Waypoint> list = waypoints.get();
        String waypointName = name == null ? WaypointStore.nextQuickName(list) : name.strip();
        Optional<String> problem = validateName(waypointName);
        if (problem.isPresent()) {
            ChatFeedback.error(problem.get());
            return Optional.empty();
        }
        if (WaypointStore.find(list, waypointName).isPresent()) {
            ChatFeedback.error("A waypoint called \"%s\" already exists.".formatted(waypointName));
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
        if (getConfig().waypointConfig.showInWorld) {
            ChatFeedback.success(message);
        } else {
            ChatFeedback.warning(message.append(" Turn on Show In World in the Waypoints config to see it."));
        }

        return Optional.of(waypoint);
    }

    public static boolean remove(String name) {
        Optional<List<Waypoint>> waypoints = WaypointStore.current();
        Optional<Waypoint> waypoint = waypoints.flatMap(list -> WaypointStore.find(list, name));
        if (waypoint.isEmpty()) {
            ChatFeedback.error("There is no waypoint called \"%s\".".formatted(name));
            return false;
        }

        waypoints.get().remove(waypoint.get());
        WaypointStore.save();
        ChatFeedback.success(Component.literal("Removed waypoint ").append(name(waypoint.get())).append("."));
        return true;
    }

    /** Why {@code name} cannot be a waypoint name, or empty if it can. */
    public static Optional<String> validateName(String name) {
        if (name.isEmpty()) return Optional.of("A waypoint needs a name.");
        if (name.length() > MAX_NAME_LENGTH) return Optional.of("Waypoint names can be at most %d characters long.".formatted(MAX_NAME_LENGTH));
        if (name.chars().anyMatch(character -> character == '\u00a7' || character == '"' || Character.isISOControl(character))) {
            return Optional.of("Waypoint names cannot contain \u00a7 or \".");
        }
        return Optional.empty();
    }

    /** "Overworld", "Nether" or "The End" for the vanilla dimensions, otherwise the dimension's ID. */
    public static String dimensionName(String dimension) {
        return switch (dimension) {
            case "minecraft:overworld" -> "Overworld";
            case "minecraft:the_nether" -> "Nether";
            case "minecraft:the_end" -> "The End";
            default -> dimension;
        };
    }

    /** The waypoint's name in its colour. */
    public static MutableComponent name(Waypoint waypoint) {
        return Component.literal(waypoint.name).withStyle(style -> style.withColor(TextColor.fromRgb(waypoint.colour)));
    }
}
