package io.github.brainage04.brainagehud.waypoint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class WaypointActionsTest {
    @Test
    void namesUpToTheMaximumLengthAreAccepted() {
        assertEquals(Optional.empty(), WaypointActions.validateName("a".repeat(64)));
        assertEquals(Optional.of("Waypoint names can be at most 64 characters long."), WaypointActions.validateName("a".repeat(65)));
    }

    @Test
    void namesWithFormattingCodesQuotesOrControlCharactersAreRejected() {
        for (String name : new String[]{"\u00a7cRed", "My \"Base\"", "Tab\there"}) {
            assertEquals(Optional.of("Waypoint names cannot contain \u00a7 or \"."), WaypointActions.validateName(name), name);
        }
        assertTrue(WaypointActions.validateName("").isPresent());
    }

    @Test
    void vanillaDimensionsHaveNamesAndOthersKeepTheirId() {
        assertEquals("Overworld", WaypointActions.dimensionName("minecraft:overworld"));
        assertEquals("Nether", WaypointActions.dimensionName("minecraft:the_nether"));
        assertEquals("The End", WaypointActions.dimensionName("minecraft:the_end"));
        assertEquals("mymod:moon", WaypointActions.dimensionName("mymod:moon"));
    }
}
