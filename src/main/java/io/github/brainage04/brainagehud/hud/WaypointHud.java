package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.WaypointHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class WaypointHud implements BasicCoreHudElement<WaypointHudConfig> {
    private static final String[] DIRECTION_ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientPacketListener connection = client.getConnection();
        if (player == null || client.level == null || connection == null) {
            return lines;
        }

        List<WaypointEntry> entries = new ArrayList<>();
        connection.getWaypointManager().forEachWaypoint(player, waypoint -> entries.add(new WaypointEntry(
                identity(connection, waypoint),
                waypoint.distanceSquared(player),
                waypoint.yawAngleToCamera(
                        client.level,
                        client.gameRenderer.mainCamera(),
                        entity -> client.getDeltaTracker().getGameTimeDeltaPartialTick(
                                !client.level.tickRateManager().isEntityFrozen(entity)
                        )
                )
        )));
        entries.sort(Comparator.comparingDouble(WaypointEntry::distanceSquared).thenComparing(WaypointEntry::identity));

        int limit = Math.clamp(getElementConfig().maximumEntries, 1, 20);
        for (int index = 0; index < entries.size() && index < limit; index++) {
            WaypointEntry entry = entries.get(index);
            lines.add("%s: %s blocks %s".formatted(
                    entry.identity(), roundedDistance(entry.distanceSquared()), directionArrow(entry.relativeYaw())
            ));
        }
        return lines;
    }

    static String directionArrow(double relativeYaw) {
        int index = Math.floorMod((int) Math.floor((relativeYaw + 22.5D) / 45.0D), DIRECTION_ARROWS.length);
        return DIRECTION_ARROWS[index];
    }

    static String roundedDistance(double distanceSquared) {
        if (!Double.isFinite(distanceSquared)) {
            return "∞";
        }
        return Long.toString(Math.round(Math.sqrt(Math.max(0.0D, distanceSquared))));
    }

    private static String identity(ClientPacketListener connection, TrackedWaypoint waypoint) {
        return waypoint.id().map(uuid -> playerNameOrId(connection, uuid), String::valueOf);
    }

    private static String playerNameOrId(ClientPacketListener connection, UUID uuid) {
        PlayerInfo playerInfo = connection.getPlayerInfo(uuid);
        return playerInfo == null ? conciseUuid(uuid) : playerInfo.getProfile().name();
    }

    private static String conciseUuid(UUID uuid) {
        return uuid.toString().substring(0, 8);
    }

    @Override
    public WaypointHudConfig getElementConfig() {
        return getConfig().waypointHudConfig;
    }

    private record WaypointEntry(String identity, double distanceSquared, double relativeYaw) {
    }
}
