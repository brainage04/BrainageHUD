package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.brainagehud.util.MathUtils;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class MotionHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Motion HUD", false, -5, 200, ElementAnchor.TOP_RIGHT);
    @ConfigEntry.Gui.Tooltip @ConfigEntry.BoundedDiscrete(min = 0, max = MathUtils.MAX_DECIMAL_PLACES) public int decimalPlaces = 2;
    public boolean showAxes = true;
    public boolean showHorizontalSpeed = true;
    @ConfigEntry.Gui.Tooltip public boolean showBlocksPerTick = false;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
