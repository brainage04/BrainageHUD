package com.github.brainage04.brainagehud.config;

import com.github.brainage04.brainagehud.BrainageHUD;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = BrainageHUD.MOD_ID)
public class BrainageHUDConfig implements ConfigData {
    public int primaryTextColour = 0xffffff;
    public int secondaryTextColour = 0x7f7fff;
    public int disabledTextColour = 0x7f7f7f;
    public int highlightedElementColour = 0x7fffffff;
    public int elementPadding = 2;
    public int screenMargin = 10;
    public boolean textShadows = true;
    public boolean backdrop = false;
    public int backdropColour = 0x7f000000;

    @ConfigEntry.Gui.CollapsibleObject
    public QualityOfLifeImprovements qualityOfLifeImprovementsConfig = new QualityOfLifeImprovements();

    public static class QualityOfLifeImprovements {
        @ConfigEntry.Gui.Tooltip(count = 4)
        public double gamma;

        public QualityOfLifeImprovements() {
            this.gamma = 3;
        }
    }

    @ConfigEntry.Gui.CollapsibleObject
    public PositionHUDConfig positionHudConfig = new PositionHUDConfig();

    public static class PositionHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showPosition;
        public int positionDecimalPlaces;
        public boolean cCounter;
        public boolean eCounter;
        public boolean showDirection;
        public boolean showRotation;
        public int rotationDecimalPlaces;

        public PositionHUDConfig() {
            this.coreSettings = new CoreSettings(true, 10, 10, ElementAnchor.TOPLEFT, 0, "Position HUD");
            this.showPosition = true;
            this.positionDecimalPlaces = 1;
            this.cCounter = true;
            this.eCounter = true;
            this.showDirection = true;
            this.showRotation = true;
            this.rotationDecimalPlaces = 2;
        }
    }

    @ConfigEntry.Gui.CollapsibleObject
    public DateTimeHUDConfig dateTimeHudConfig = new DateTimeHUDConfig();

    public static class DateTimeHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showDate;
        public boolean showTime;
        public boolean twelveHourFormat;
        public boolean showTimezone;

        public DateTimeHUDConfig() {
            this.coreSettings = new CoreSettings(true, 10, 10, ElementAnchor.TOPRIGHT, 1, "Date and Time HUD");
            this.showDate = true;
            this.showTime = true;
            this.twelveHourFormat = true;
            this.showTimezone = true;
        }
    }

    @ConfigEntry.Gui.CollapsibleObject
    public ToggleSprintHUDConfig toggleSprintHudConfig = new ToggleSprintHUDConfig();

    public static class ToggleSprintHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;

        public ToggleSprintHUDConfig() {
            this.coreSettings = new CoreSettings(true, 10, 10, ElementAnchor.BOTTOMLEFT, 2, "Toggle Sprint HUD");
        }
    }

    @ConfigEntry.Gui.CollapsibleObject
    public PerformanceHUDConfig performanceHudConfig = new PerformanceHUDConfig();

    public static class PerformanceHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showFps;
        public boolean showRamUsage;
        public boolean showCpuUsage;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean showGpuUsage;

        public PerformanceHUDConfig() {
            this.coreSettings = new CoreSettings(true, 10, 90, ElementAnchor.TOPLEFT, 3, "Performance HUD");
            this.showFps = true;
            this.showRamUsage = false;
            this.showCpuUsage = false;
            this.showGpuUsage = false;
        }
    }

    @ConfigEntry.Gui.CollapsibleObject
    public NetworkHUDConfig networkHudConfig = new NetworkHUDConfig();

    public static class NetworkHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showPing;
        public boolean showTps;
        public int tpsDecimalPlaces;
        public boolean showNetworkActivity;

        public NetworkHUDConfig() {
            this.coreSettings = new CoreSettings(true, 10, 170, ElementAnchor.TOPLEFT, 4, "Network HUD");
            this.showPing = true;
            this.showTps = false;
            this.tpsDecimalPlaces = 1;
            this.showNetworkActivity = false;
        }
    }

    public static class CoreSettings {
        public boolean enabled;
        public int x;
        public int y;
        public ElementAnchor elementAnchor;
        @ConfigEntry.Gui.Excluded
        public int elementId;
        @ConfigEntry.Gui.Excluded
        public String elementName;

        public CoreSettings(boolean enabled, int x, int y, ElementAnchor elementAnchor, int elementId, String elementName) {
            this.enabled = enabled;
            this.x = x;
            this.y = y;
            this.elementAnchor = elementAnchor;
            this.elementId = elementId;
            this.elementName = elementName;
        }
    }

    public enum ElementAnchor {
        TOPLEFT,
        TOP,
        TOPRIGHT,
        LEFT,
        CENTER,
        RIGHT,
        BOTTOMLEFT,
        BOTTOM,
        BOTTOMRIGHT
    }
}
