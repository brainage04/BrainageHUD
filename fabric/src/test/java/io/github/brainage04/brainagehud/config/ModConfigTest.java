package io.github.brainage04.brainagehud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.config.core.ModConfig;
import org.junit.jupiter.api.Test;

class ModConfigTest {
    @Test
    void handEditedValuesCannotCreateZeroDivisorsOrInvalidRenderingRanges() {
        ModConfig config = new ModConfig();
        config.networkHudConfig.updatePingTickInterval = 0;
        config.networkHudConfig.pingIntervalsTracked = -5;
        config.networkHudConfig.tpsIntervalsTracked = -1;
        config.positionHudConfig.positionDecimalPlaces = -1;
        config.waypointConfig.labelScalePercent = -100;
        config.keystrokesHudConfig.keyBackdropOpacity = -1;
        config.qualityOfLifeConfig.fullbright = -5;

        config.validatePostLoad();

        assertEquals(1, config.networkHudConfig.updatePingTickInterval);
        assertEquals(1, config.networkHudConfig.pingIntervalsTracked);
        assertEquals(1, config.networkHudConfig.tpsIntervalsTracked);
        assertEquals(0, config.positionHudConfig.positionDecimalPlaces);
        assertEquals(50, config.waypointConfig.labelScalePercent);
        assertEquals(0, config.keystrokesHudConfig.keyBackdropOpacity);
        assertEquals(-1, config.qualityOfLifeConfig.fullbright);
    }

    @Test
    void validCustomSettingsSurvivePostLoadValidation() {
        ModConfig config = new ModConfig();
        config.networkHudConfig.updatePingTickInterval = 7;
        config.networkHudConfig.tpsIntervalsTracked = 9;
        config.motionHudConfig.decimalPlaces = 3;
        config.waypointConfig.labelScalePercent = 175;
        config.qualityOfLifeConfig.fullbright = 0.5F;

        config.validatePostLoad();

        assertEquals(7, config.networkHudConfig.updatePingTickInterval);
        assertEquals(9, config.networkHudConfig.tpsIntervalsTracked);
        assertEquals(3, config.motionHudConfig.decimalPlaces);
        assertEquals(175, config.waypointConfig.labelScalePercent);
        assertEquals(0.5F, config.qualityOfLifeConfig.fullbright);
    }
}
