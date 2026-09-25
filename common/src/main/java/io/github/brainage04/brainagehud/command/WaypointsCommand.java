package io.github.brainage04.brainagehud.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.util.ChatFeedback;
import io.github.brainage04.brainagehud.waypoint.Waypoint;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.brainagehud.waypoint.WaypointStore;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/**
 * {@code /waypoints}: manage the current world's waypoints. It is not called {@code /waypoint},
 * because a client command of that name would shadow vanilla's server {@code /waypoint}.
 *
 * <p>Incomplete or unexpected input runs the usage message of the subcommand it was meant for,
 * instead of failing to parse.
 */
public final class WaypointsCommand {
    private static final String USAGE = "/waypoints [add|remove|list]";
    private static final String ADD_USAGE = "/waypoints add <name> [<x> <y> <z>]";
    private static final String REMOVE_USAGE = "/waypoints remove <name>";
    private static final String LIST_USAGE = "/waypoints list";

    private WaypointsCommand() {}

    public static <S extends SharedSuggestionProvider> void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<S>literal("waypoints")
                        .executes(context -> openScreen())
                        .then(LiteralArgumentBuilder.<S>literal("add")
                                .executes(context -> usage(ADD_USAGE))
                                .then(RequiredArgumentBuilder.<S, String>argument("name", StringArgumentType.string())
                                        .executes(context -> added(WaypointActions.createAtPlayer(name(context)).isPresent()))
                                        .then(RequiredArgumentBuilder.<S, String>argument("coordinates", StringArgumentType.greedyString())
                                                .executes(context -> addAt(name(context), StringArgumentType.getString(context, "coordinates"))))))
                        .then(LiteralArgumentBuilder.<S>literal("remove")
                                .executes(context -> usage(REMOVE_USAGE))
                                .then(RequiredArgumentBuilder.<S, String>argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                WaypointStore.current().orElse(List.of()).stream()
                                                        .map(waypoint -> StringArgumentType.escapeIfRequired(waypoint.name)),
                                                builder))
                                        .executes(context -> WaypointActions.remove(name(context)) ? 1 : 0)
                                        .then(unexpected(REMOVE_USAGE))))
                        .then(LiteralArgumentBuilder.<S>literal("list")
                                .executes(context -> list())
                                .then(unexpected(LIST_USAGE)))
                        .then(unexpected(USAGE)));
    }

    /** Takes whatever is left of the command and shows {@code usage}. */
    private static <S> RequiredArgumentBuilder<S, String> unexpected(String usage) {
        return RequiredArgumentBuilder.<S, String>argument("unexpected", StringArgumentType.greedyString())
                .executes(context -> usage(usage));
    }

    private static <S> String name(CommandContext<S> context) {
        return StringArgumentType.getString(context, "name");
    }

    private static int added(boolean added) {
        return added ? 1 : 0;
    }

    private static int addAt(String name, String coordinates) {
        String[] words = coordinates.strip().split(" +");
        if (words.length != 3) return usage(ADD_USAGE);

        int[] position = new int[3];
        for (int axis = 0; axis < 3; axis++) {
            try {
                position[axis] = Integer.parseInt(words[axis]);
            } catch (NumberFormatException exception) {
                ChatFeedback.error("X, Y and Z must be whole numbers.");
                return 0;
            }
        }

        return added(WaypointActions.create(name, new BlockPos(position[0], position[1], position[2])).isPresent());
    }

    private static int usage(String usage) {
        ChatFeedback.error("Usage: " + usage);
        return 0;
    }

    private static int openScreen() {
        Minecraft minecraft = Minecraft.getInstance();
        // the chat screen closes once the command returns, so open the new screen afterwards
        minecraft.schedule(() -> minecraft.setScreenAndShow(new WaypointsScreen(null)));
        return 1;
    }

    private static int list() {
        List<Waypoint> waypoints = WaypointStore.current().orElse(List.of());
        if (waypoints.isEmpty()) {
            ChatFeedback.warning("No waypoints in this world. Add one with /waypoints add <name>.");
            return 1;
        }

        ChatFeedback.info("Waypoints:");
        for (Waypoint waypoint : waypoints) {
            ChatFeedback.detail(Component.literal(" - ")
                    .append(WaypointActions.name(waypoint))
                    .append(" %s (%s)%s".formatted(
                            waypoint.pos().toShortString(),
                            WaypointActions.dimensionName(waypoint.dimension),
                            waypoint.visible ? "" : " [hidden]")));
        }
        return 1;
    }
}
