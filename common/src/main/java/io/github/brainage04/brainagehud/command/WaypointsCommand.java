package io.github.brainage04.brainagehud.command;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
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
 */
public final class WaypointsCommand {
    private WaypointsCommand() {}

    public static <S extends SharedSuggestionProvider> void register(CommandDispatcher<S> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<S>literal("waypoints")
                        .executes(context -> openScreen())
                        .then(LiteralArgumentBuilder.<S>literal("add")
                                .then(RequiredArgumentBuilder.<S, String>argument("name", StringArgumentType.string())
                                        .executes(context -> added(WaypointActions.createAtPlayer(name(context)).isPresent()))
                                        .then(RequiredArgumentBuilder.<S, Integer>argument("x", IntegerArgumentType.integer())
                                                .then(RequiredArgumentBuilder.<S, Integer>argument("y", IntegerArgumentType.integer())
                                                        .then(RequiredArgumentBuilder.<S, Integer>argument("z", IntegerArgumentType.integer())
                                                                .executes(context -> added(WaypointActions.create(
                                                                        name(context),
                                                                        new BlockPos(
                                                                                IntegerArgumentType.getInteger(context, "x"),
                                                                                IntegerArgumentType.getInteger(context, "y"),
                                                                                IntegerArgumentType.getInteger(context, "z")))
                                                                        .isPresent())))))))
                        .then(LiteralArgumentBuilder.<S>literal("remove")
                                .then(RequiredArgumentBuilder.<S, String>argument("name", StringArgumentType.string())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                WaypointStore.current().orElse(List.of()).stream()
                                                        .map(waypoint -> StringArgumentType.escapeIfRequired(waypoint.name)),
                                                builder))
                                        .executes(context -> WaypointActions.remove(name(context)) ? 1 : 0)))
                        .then(LiteralArgumentBuilder.<S>literal("list").executes(context -> list())));
    }

    private static <S> String name(CommandContext<S> context) {
        return StringArgumentType.getString(context, "name");
    }

    private static int added(boolean added) {
        return added ? 1 : 0;
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
            feedback(Component.literal("No waypoints in this world. Add one with /waypoints add <name>."));
            return 1;
        }

        feedback(Component.literal("Waypoints:"));
        for (Waypoint waypoint : waypoints) {
            feedback(Component.literal(" - ")
                    .append(WaypointActions.name(waypoint))
                    .append(" %s (%s)%s".formatted(
                            waypoint.pos().toShortString(),
                            waypoint.dimension,
                            waypoint.visible ? "" : " [hidden]")));
        }
        return 1;
    }
}
