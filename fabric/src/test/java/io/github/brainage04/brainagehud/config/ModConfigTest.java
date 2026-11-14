package io.github.brainage04.brainagehud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.ClicksPerSecondFormat;
import io.github.brainage04.brainagehud.config.other.WaypointConfig;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Property;
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

    @Test
    void namesSavedBy103TakeTheCurrentNamesAndKeepTheirSettings() {
        ModConfig config =
                GSON.fromJson(
                        """
                        {
                          "armourInfoHudConfig": { "coreSettings": {
                            "elementName": "Armor Info HUD", "enabled": false, "x": 7, "y": -9,
                            "elementAnchor": "TOP_LEFT",
                            "elementOverrides": { "padding": { "enabled": true, "value": 6 } }
                          } },
                          "dateTimeHudConfig": { "coreSettings": {
                            "elementName": "Date Time HUD", "enabled": true, "x": 1, "y": 2,
                            "elementAnchor": "BOTTOM"
                          } },
                          "fishingHudConfig": { "coreSettings": {
                            "elementName": "My fishing panel", "enabled": true, "x": 3, "y": 4,
                            "elementAnchor": "LEFT"
                          } }
                        }
                        """,
                        ModConfig.class);

        config.validatePostLoad();

        assertEquals("Armour Info HUD", config.armourInfoHudConfig.coreSettings.elementName);
        assertFalse(config.armourInfoHudConfig.coreSettings.enabled);
        assertEquals(7, config.armourInfoHudConfig.coreSettings.x);
        assertEquals(-9, config.armourInfoHudConfig.coreSettings.y);
        assertEquals(ElementAnchor.TOP_LEFT, config.armourInfoHudConfig.coreSettings.elementAnchor);
        assertEquals(6, config.armourInfoHudConfig.coreSettings.elementOverrides.padding.value);
        assertEquals("Date/Time HUD", config.dateTimeHudConfig.coreSettings.elementName);
        assertEquals(ElementAnchor.BOTTOM, config.dateTimeHudConfig.coreSettings.elementAnchor);
        assertEquals("My fishing panel", config.fishingHudConfig.coreSettings.elementName);
    }

    @Test
    void aSectionSetToNullLoadsAsItsDefaultAndIsLoggedOnce() {
        ModConfig config =
                GSON.fromJson(
                        """
                        {
                          "networkHudConfig": null,
                          "waypointConfig": null,
                          "fishingHudConfig": { "coreSettings": {
                            "elementName": "Fishing HUD", "enabled": false, "x": 3, "y": 4,
                            "elementAnchor": "LEFT"
                          } }
                        }
                        """,
                        ModConfig.class);

        List<String> warnings = warningsDuring(config::validatePostLoad);

        assertEquals(GSON.toJson(new NetworkHudConfig()), GSON.toJson(config.networkHudConfig));
        assertEquals(GSON.toJson(new WaypointConfig()), GSON.toJson(config.waypointConfig));
        assertFalse(config.fishingHudConfig.coreSettings.enabled);
        assertEquals(
                List.of(
                        "Config value ModConfig.networkHudConfig is missing, using the default",
                        "Config value ModConfig.waypointConfig is missing, using the default"),
                warnings.stream().sorted().toList());
    }

    private static List<String> warningsDuring(Runnable action) {
        Logger logger = (Logger) LogManager.getLogger(BrainageHUD.MOD_ID);
        List<String> warnings = new ArrayList<>();
        AbstractAppender appender =
                new AbstractAppender("ModConfigTest", null, null, true, Property.EMPTY_ARRAY) {
                    @Override
                    public void append(LogEvent event) {
                        if (event.getLevel() == Level.WARN) {
                            warnings.add(event.getMessage().getFormattedMessage());
                        }
                    }
                };
        appender.start();
        logger.addAppender(appender);
        Level level = logger.getLevel();
        Configurator.setLevel(BrainageHUD.MOD_ID, Level.WARN);
        try {
            action.run();
        } finally {
            Configurator.setLevel(BrainageHUD.MOD_ID, level);
            logger.removeAppender(appender);
            appender.stop();
        }
        return warnings;
    }
}
