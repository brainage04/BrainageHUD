package io.github.brainage04.brainagehud.waypoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class WaypointStoreTest {
    @Test
    void quickNamesFillTheFirstGapIgnoringCase() {
        List<Waypoint> waypoints =
                List.of(waypoint("Waypoint 1"), waypoint("waypoint 3"), waypoint("Base"));

        assertEquals("Waypoint 2", WaypointStore.nextQuickName(waypoints));
        assertEquals(
                "Waypoint 4",
                WaypointStore.nextQuickName(
                        List.of(
                                waypoint("Waypoint 1"),
                                waypoint("Waypoint 2"),
                                waypoint("WAYPOINT 3"))));
    }

    @Test
    void aWaypointWithoutVisibleLoadsVisible() {
        // The mod writes visible for every waypoint, so only a hand-edited or partial file leaves
        // it out; such a waypoint must still load visible, not hidden.
        String saved =
                """
                {
                  "server/example.org": [
                    {
                      "name": "Base",
                      "x": 10,
                      "y": 64,
                      "z": -20,
                      "dimension": "minecraft:overworld",
                      "colour": 16777215
                    }
                  ]
                }
                """;

        List<Waypoint> loaded =
                WaypointStore.read(new StringReader(saved)).get("server/example.org");

        assertNotNull(loaded);
        assertTrue(loaded.get(0).visible);
    }

    private static Waypoint waypoint(String name) {
        return new Waypoint(name, BlockPos.ZERO, "minecraft:overworld", 0xFFFFFF);
    }
}
