package com.github.brainage04.brainagehud.config;

import com.github.brainage04.brainagehud.BrainageHUD;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings({"unused"})
@Config(name = BrainageHUD.MOD_ID)
public class BrainageHUDConfig implements ConfigData {
    public boolean textShadows = true;
    @ConfigEntry.ColorPicker public int primaryTextColour = 0xffffff;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 255) public int backdropOpacity = 0;

    public int elementPadding = 2;
    public int screenMargin = 5;

    @ConfigEntry.Gui.CollapsibleObject public QualityOfLifeImprovements qualityOfLifeImprovementsConfig = new QualityOfLifeImprovements();

    @ConfigEntry.Gui.CollapsibleObject public ArmourInfoHUDConfig armourInfoHudConfig = new ArmourInfoHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public DateTimeHUDConfig dateTimeHudConfig = new DateTimeHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public KeystrokesHUDConfig keystrokesHudConfig = new KeystrokesHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public KillDeathRatioHUDConfig killDeathRatioHudConfig = new KillDeathRatioHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public NetworkHUDConfig networkHudConfig = new NetworkHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public PerformanceHUDConfig performanceHudConfig = new PerformanceHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public PositionHUDConfig positionHudConfig = new PositionHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public ToggleSprintHUDConfig toggleSprintHudConfig = new ToggleSprintHUDConfig();

    public static class QualityOfLifeImprovements {
        @ConfigEntry.Gui.Tooltip(count = 4)
        public double gamma;

        public QualityOfLifeImprovements() {
            this.gamma = 3;
        }
    }

    public static class ArmourInfoHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showArmour;
        public boolean showMainHand;
        public boolean showOffHand;
        public boolean showItemNames;
        public boolean showDurabilityBar;
        public DurabilityFormat durabilityFormat;
        public int durabilityDecimalPlaces;

        public ArmourInfoHUDConfig() {
            this.coreSettings = new CoreSettings(0, "Armor Info HUD", true, -135, 5, ElementAnchor.BOTTOMRIGHT, false, 100);
            this.showArmour = true;
            this.showMainHand = false;
            this.showOffHand = false;
            this.showItemNames = false;
            this.showDurabilityBar = true;
            this.durabilityFormat = DurabilityFormat.ONE_NUMBER;
            this.durabilityDecimalPlaces = 1;
        }
    }

    public static class DateTimeHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showDate;
        public boolean showTime;
        public boolean twelveHourFormat;
        public boolean showTimezone;

        public DateTimeHUDConfig() {
            this.coreSettings = new CoreSettings(1, "Date Time HUD", true, -85, 5, ElementAnchor.TOPRIGHT, false, 100);
            this.showDate = true;
            this.showTime = true;
            this.twelveHourFormat = true;
            this.showTimezone = true;
        }
    }

    public static class KeystrokesHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public int keyBackdropOpacity;
        public boolean showWasd;
        public boolean showSpace;
        public boolean showMouseButtons;
        public ClicksPerSecondFormat clicksPerSecondFormat;

        public KeystrokesHUDConfig() {
            this.coreSettings = new CoreSettings(2, "Keystrokes HUD", true, -5, 5, ElementAnchor.TOPRIGHT, true, 0);
            this.keyBackdropOpacity = 100;
            this.showWasd = true;
            this.showSpace = true;
            this.showMouseButtons = true;
            this.clicksPerSecondFormat = ClicksPerSecondFormat.LEFT_CLICK;
        }
    }

    public static class KillDeathRatioHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public KillDeathRatioFormat killDeathRatioFormat;
        @ConfigEntry.Gui.Excluded
        public int kills;
        @ConfigEntry.Gui.Excluded
        public int deaths;

        public KillDeathRatioHUDConfig() {
            this.coreSettings = new CoreSettings(3, "Kill Death Ratio HUD", true, 5, 305, ElementAnchor.TOPLEFT, false, 100);
            this.killDeathRatioFormat = KillDeathRatioFormat.BOTH_NUMBERS;
            this.kills = 0;
            this.deaths = 0;
        }
    }

    public static class NetworkHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showPing;
        public boolean showTps;
        public int tpsDecimalPlaces;

        public NetworkHUDConfig() {
            this.coreSettings = new CoreSettings(4, "Network HUD", true, 5, 205, ElementAnchor.TOPLEFT, false, 100);
            this.showPing = true;
            this.showTps = false;
            this.tpsDecimalPlaces = 1;
        }
    }

    public static class PerformanceHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;
        public boolean showFps;
        public boolean showRamUsage;
        public boolean showCpuUsage;
        @ConfigEntry.Gui.Tooltip(count = 3)
        public boolean showGpuUsage;

        public PerformanceHUDConfig() {
            this.coreSettings = new CoreSettings(5, "Performance HUD", true, 5, 105, ElementAnchor.TOPLEFT, false, 100);
            this.showFps = true;
            this.showRamUsage = false;
            this.showCpuUsage = false;
            this.showGpuUsage = false;
        }
    }

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
            this.coreSettings = new CoreSettings(6, "Position HUD", true, 5, 5, ElementAnchor.TOPLEFT, false, 100);
            this.showPosition = true;
            this.positionDecimalPlaces = 1;
            this.cCounter = true;
            this.eCounter = true;
            this.showDirection = true;
            this.showRotation = true;
            this.rotationDecimalPlaces = 2;
        }
    }

    public static class ToggleSprintHUDConfig {
        @ConfigEntry.Gui.CollapsibleObject
        public CoreSettings coreSettings;

        public ToggleSprintHUDConfig() {
            this.coreSettings = new CoreSettings(7, "Toggle Sprint HUD", true, 5, -5, ElementAnchor.BOTTOMLEFT, false, 100);
        }
    }

    public static class CoreSettings {
        @ConfigEntry.Gui.Excluded
        public int elementId;
        @ConfigEntry.Gui.Excluded
        public String elementName;

        public boolean enabled;
        public int x;
        public int y;
        public ElementAnchor elementAnchor;

        public boolean overrideGlobalBackdropOpacity;
        @ConfigEntry.BoundedDiscrete(min = 0, max = 255) public int backdropOpacity;

        public CoreSettings(int elementId, String elementName, boolean enabled, int x, int y, ElementAnchor elementAnchor, boolean overrideGlobalBackdropOpacity, int backdropOpacity) {
            this.elementId = elementId;
            this.elementName = elementName;

            this.enabled = enabled;
            this.x = x;
            this.y = y;
            this.elementAnchor = elementAnchor;

            this.overrideGlobalBackdropOpacity = overrideGlobalBackdropOpacity;
            this.backdropOpacity = backdropOpacity;
        }
    }

    public enum ElementAnchor {
        TOPLEFT("Top Left"),
        TOP("Top"),
        TOPRIGHT("Top Right"),
        LEFT("Left"),
        CENTER("Center"),
        RIGHT("Right"),
        BOTTOMLEFT("Bottom Left"),
        BOTTOM("Bottom"),
        BOTTOMRIGHT("Bottom Right");

        private final String name;

        ElementAnchor(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum ClicksPerSecondFormat {
        NONE("None"),
        LEFT_CLICK("Left Click"),
        RIGHT_CLICK("Right Click"),
        BOTH("Both");

        private final String name;

        ClicksPerSecondFormat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum DurabilityFormat {
        NONE("None"),
        ONE_NUMBER("One Number"),
        BOTH_NUMBERS("Both Numbers"),
        PERCENTAGE("Percentage");

        private final String name;

        DurabilityFormat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public enum KillDeathRatioFormat {
        BOTH_NUMBERS("Both Numbers"),
        SIMPLIFIED("Simplified");

        private final String name;

        KillDeathRatioFormat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
