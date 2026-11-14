package io.github.brainage04.brainagehud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.ClicksPerSecondFormat;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import org.junit.jupiter.api.Test;

class ModConfigTest {
    // The Gson that Cloth Config's GsonConfigSerializer(Config, Class) builds (cloth-config
    // 26.3.159).
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    @Test
    void misspeltFormatsFallBackToTheDefaults() {
        // Gson reads an enum name it does not know as null, and both HUDs switch on the format
        // every frame.
        ModConfig config =
                GSON.fromJson(
                        """
                        {
                          "keystrokesHudConfig": { "clicksPerSecondFormat": "Left_Click" },
                          "armourInfoHudConfig": { "durabilityFormat": "PERCENT" }
                        }
                        """,
                        ModConfig.class);

        config.validatePostLoad();

        assertEquals(
                ClicksPerSecondFormat.LEFT_CLICK, config.keystrokesHudConfig.clicksPerSecondFormat);
        assertEquals(DurabilityFormat.FIRST_NUMBER, config.armourInfoHudConfig.durabilityFormat);
    }

    @Test
    void aSavedFileLoadsUnchanged() {
        ModConfig saved = new ModConfig();
        saved.keystrokesHudConfig.clicksPerSecondFormat = ClicksPerSecondFormat.BOTH;
        saved.armourInfoHudConfig.durabilityFormat = DurabilityFormat.PERCENTAGE;
        saved.armourInfoHudConfig.coreSettings.elementAnchor = ElementAnchor.TOP_LEFT;
        saved.armourInfoHudConfig.coreSettings.x = 12;
        saved.networkHudConfig.updatePingTickInterval = 7;
        saved.fishingHudConfig.coreSettings.enabled = false;
        saved.fishingHudConfig.coreSettings.elementOverrides.maxWidth.enabled = true;
        saved.fishingHudConfig.coreSettings.elementOverrides.maxWidth.value = 150;
        String file = GSON.toJson(saved);

        ModConfig loaded = GSON.fromJson(file, ModConfig.class);
        loaded.validatePostLoad();

        assertEquals(file, GSON.toJson(loaded));
    }
}
