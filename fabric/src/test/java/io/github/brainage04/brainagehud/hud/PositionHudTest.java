package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PositionHudTest {
    @Test
    void labelsEachSectorCentre() {
        assertLabels(
                Map.of(
                        0.0F, "S (+Z)",
                        45.0F, "SW (-X, +Z)",
                        90.0F, "W (-X)",
                        135.0F, "NW (-X, -Z)",
                        180.0F, "N (-Z)",
                        -180.0F, "N (-Z)",
                        -135.0F, "NE (+X, -Z)",
                        -90.0F, "E (+X)",
                        -45.0F, "SE (+X, +Z)"));
    }

    @Test
    void switchesLabelsOnSectorBoundaries() {
        assertLabels(
                Map.of(
                        22.5F, "SW (-X, +Z)",
                        22.49F, "S (+Z)",
                        -22.5F, "S (+Z)",
                        -22.51F, "SE (+X, +Z)",
                        157.5F, "N (-Z)",
                        -157.51F, "N (-Z)"));
    }

    @Test
    void recognisesFarmingToolsByWholeWordsInTheirName() {
        assertTrue(PositionHud.isFarmingToolName("Melon Dicer 3.0"));
        assertTrue(PositionHud.isFarmingToolName("Euclid's Wheat Hoe"));
        assertTrue(PositionHud.isFarmingToolName("Cactus Knife"));
        assertTrue(PositionHud.isFarmingToolName("Rookie Axe"));
        assertFalse(PositionHud.isFarmingToolName("Diamond Pickaxe"));
        assertFalse(PositionHud.isFarmingToolName("Shovel"));
    }

    @Test
    void takesTheCCounterFromVanillasSectionStatistics() {
        // the form of LevelExtractor.sectionStatistics() in 26.3
        String statistics = "C: 4/6168 (s) D: 12, pC: 000, aB: 12";
        assertEquals("C: 4/6168 (s)", PositionHud.getCCounter(statistics, true));
        assertEquals("C: 4/6168", PositionHud.getCCounter(statistics, false));
    }

    @Test
    void takesTheCCounterFromSodiumsSectionStatistics() {
        // the form of RenderSectionManager.getChunksDebugString() in Sodium 0.9.3
        assertEquals("C: 4/6168 (s)", PositionHud.getCCounter("C: 4/6168 (R) D: 12", true));
    }

    @Test
    void showsSectionStatisticsWithoutCountsUnchanged() {
        assertEquals("Chunks: 4", PositionHud.getCCounter("Chunks: 4", true));
        assertNull(PositionHud.getCCounter(null, true));
    }

    private static void assertLabels(Map<Float, String> expectedLabels) {
        assertAll(
                expectedLabels.entrySet().stream()
                        .map(
                                expected ->
                                        () ->
                                                assertEquals(
                                                        expected.getValue(),
                                                        PositionHud.getYawString(expected.getKey()),
                                                        "yaw " + expected.getKey())));
    }
}
