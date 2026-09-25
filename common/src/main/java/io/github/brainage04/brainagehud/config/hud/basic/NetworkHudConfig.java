package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.brainagehud.util.MathUtils;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class NetworkHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Network HUD", true, 5, 116, ElementAnchor.TOP_LEFT);
    public boolean showPing = true;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20) public int updatePingTickInterval = 10;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int pingIntervalsTracked = 3;
    public boolean showTps = true;
    @ConfigEntry.Gui.Tooltip public boolean colourValues = true;
    @ConfigEntry.Gui.Tooltip @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int tpsIntervalsTracked = 3;
    @ConfigEntry.BoundedDiscrete(min = 0, max = MathUtils.MAX_DECIMAL_PLACES) public int tpsDecimalPlaces = 1;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
