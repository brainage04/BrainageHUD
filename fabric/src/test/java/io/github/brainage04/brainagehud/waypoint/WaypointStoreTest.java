package io.github.brainage04.brainagehud.waypoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
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

    @Test
    void readDropsEntriesWithoutANameOrDimension() {
        // find() compares names and the renderer compares dimensions, so a hand-edited entry
        // without either would throw instead of being skipped.
        String saved =
                """
                {
                  "server/example.org": [
                    {
                      "name": "Base",
                      "x": 1,
                      "y": 64,
                      "z": 2,
                      "dimension": "minecraft:overworld",
                      "colour": 16777215,
                      "visible": true
                    },
                    {
                      "x": 1,
                      "y": 64,
                      "z": 2,
                      "dimension": "minecraft:overworld",
                      "colour": 16777215
                    },
                    {
                      "name": "No dimension",
                      "x": 1,
                      "y": 64,
                      "z": 2,
                      "colour": 16777215
                    }
                  ]
                }
                """;

        List<String> names =
                WaypointStore.read(new StringReader(saved)).get("server/example.org").stream()
                        .map(waypoint -> waypoint.name)
                        .toList();

        assertEquals(List.of("Base"), names);
    }

    @Test
    void readSkipsAWorldWhoseListIsNull() {
        // A hand-edited file can map a world to null; load() then sets worlds to a fresh map, so a
        // throw here would erase the file on the next save.
        String saved =
                """
                {
                  "server/example.org": null,
                  "server/other.org": [
                    {
                      "name": "Base",
                      "x": 1,
                      "y": 64,
                      "z": 2,
                      "dimension": "minecraft:overworld",
                      "colour": 16777215
                    }
                  ]
                }
                """;

        Map<String, List<Waypoint>> worlds = WaypointStore.read(new StringReader(saved));

        assertEquals(1, worlds.size());
        assertNotNull(worlds.get("server/other.org"));
    }

    private static Waypoint waypoint(String name) {
        return new Waypoint(name, BlockPos.ZERO, "minecraft:overworld", 0xFFFFFF);
    }
}
