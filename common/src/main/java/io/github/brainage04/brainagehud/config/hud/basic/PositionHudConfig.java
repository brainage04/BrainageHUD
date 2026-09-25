package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.brainagehud.util.MathUtils;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PositionHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Position HUD", true, 5, 5, ElementAnchor.TOP_LEFT);
    public boolean showPosition = true;
    @ConfigEntry.Gui.Tooltip @ConfigEntry.BoundedDiscrete(min = 0, max = MathUtils.MAX_DECIMAL_PLACES) public int positionDecimalPlaces = 1;
    @ConfigEntry.Gui.Tooltip public boolean showChunkPosition = true;
    @ConfigEntry.Gui.Tooltip public boolean cCounter = true;
    @ConfigEntry.Gui.Tooltip public boolean eCounter = true;
    @ConfigEntry.Gui.Tooltip public boolean showDirection = true;
    public boolean showRotation = true;
    @ConfigEntry.Gui.Tooltip public boolean showTrueYaw = false;
    @ConfigEntry.Gui.Tooltip public boolean rotationOnlyWithFarmingTool = false;
    @ConfigEntry.Gui.Tooltip @ConfigEntry.BoundedDiscrete(min = 0, max = MathUtils.MAX_DECIMAL_PLACES) public int rotationDecimalPlaces = 2;
    @ConfigEntry.Gui.Tooltip public boolean showLight = true;
    @ConfigEntry.Gui.Tooltip public boolean showBiome = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
