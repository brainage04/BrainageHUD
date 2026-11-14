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
    void anAddressWithTheDefaultPortSharesTheWaypointsOfTheBareAddress() {
        // The game connects to "host" and "host:25565" alike, SRV lookup included, so waypoints
        // saved under either key belong to one server. Another port is another server.
        String saved =
                """
                {
                  "server/play.example.net": [%s, %s],
                  "server/play.example.net:25565": [%s, %s],
                  "server/play.example.net:25570": [%s],
                  "server/[::1]:25565": [%s],
                  "singleplayer/New World": [%s]
                }
                """
                        .formatted(
                                json("Base"),
                                json("Farm"),
                                json("Mine"),
                                json("base"),
                                json("Other"),
                                json("Local"),
                                json("Home"));

        Map<String, List<Waypoint>> loaded = WaypointStore.read(new StringReader(saved));

        assertEquals(
                List.of(
                        "server/play.example.net",
                        "server/play.example.net:25570",
                        "server/[::1]",
                        "singleplayer/New World"),
                List.copyOf(loaded.keySet()));
        assertEquals(
                List.of("Base", "Farm", "Mine", "base (2)"),
                loaded.get("server/play.example.net").stream()
                        .map(waypoint -> waypoint.name)
                        .toList());
        assertEquals("Other", loaded.get("server/play.example.net:25570").get(0).name);
        assertEquals("Local", loaded.get("server/[::1]").get(0).name);
    }

    @Test
    void theServerKeyDropsOnlyTheDefaultPort() {
        assertEquals("server/play.example.net", WaypointStore.serverKey("Play.Example.NET:25565"));
        assertEquals("server/play.example.net", WaypointStore.serverKey("play.example.net"));
        assertEquals(
                "server/play.example.net:25570", WaypointStore.serverKey("play.example.net:25570"));
        assertEquals("server/[::1]", WaypointStore.serverKey("[::1]:25565"));
        assertEquals("server/127.0.0.1:25566", WaypointStore.serverKey("127.0.0.1:25566"));
    }

    private static String json(String name) {
        return """
                { "name": "%s", "x": 1, "y": 64, "z": 2, "dimension": "minecraft:overworld",
                  "colour": 16777215, "visible": true }
                """
                .formatted(name);
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
