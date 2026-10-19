package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.config.hud.basic.MotionHudConfig;
import java.text.DecimalFormatSymbols;
import org.junit.jupiter.api.Test;

class MotionHudTest {
    private static final char SEPARATOR = DecimalFormatSymbols.getInstance().getDecimalSeparator();

    @Test
    void convertsActualDisplacementToBlocksPerSecond() {
        MotionHudConfig config = new MotionHudConfig();
        config.decimalPlaces = 2;
        config.showBlocksPerTick = false;

        assertEquals("Motion X: -5" + SEPARATOR + "00 m/s", MotionHud.speed("Motion X", -0.25, config));
        assertEquals("Speed: 0" + SEPARATOR + "00 m/s", MotionHud.speed("Speed", 0, config));
    }

    @Test
    void optionalTickValueUsesTheSameDisplacement() {
        MotionHudConfig config = new MotionHudConfig();
        config.decimalPlaces = 2;
        config.showBlocksPerTick = true;

        assertEquals("Speed: 5" + SEPARATOR + "00 m/s (0" + SEPARATOR + "25 m/tick)",
                MotionHud.speed("Speed", 0.25, config));
    }
}
