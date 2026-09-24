package io.github.brainage04.brainagehud.waypoint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class WaypointStoreTest {
    @Test
    void quickNamesFillTheFirstGapIgnoringCase() {
        List<Waypoint> waypoints = List.of(waypoint("Waypoint 1"), waypoint("waypoint 3"), waypoint("Base"));

        assertEquals("Waypoint 2", WaypointStore.nextQuickName(waypoints));
        assertEquals("Waypoint 4", WaypointStore.nextQuickName(List.of(
                waypoint("Waypoint 1"), waypoint("Waypoint 2"), waypoint("WAYPOINT 3"))));
    }

    private static Waypoint waypoint(String name) {
        return new Waypoint(name, BlockPos.ZERO, "minecraft:overworld", 0xFFFFFF);
    }
}
