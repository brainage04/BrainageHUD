package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class NetworkHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showPing;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20) public int updatePingTickInterval;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int pingIntervalsTracked;
    public boolean showTps;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20) public int updateTpsTickInterval;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int tpsIntervalsTracked;
    public int tpsDecimalPlaces;

    public NetworkHudConfig() {
        this.coreSettings = new CoreSettings(3, "Network HUD", true, 5, 94, ElementAnchor.TOP_LEFT);
        this.showPing = true;
        this.updatePingTickInterval = 10;
        this.pingIntervalsTracked = 3;
        this.showTps = true;
        this.updateTpsTickInterval = 10;
        this.tpsIntervalsTracked = 3;
        this.tpsDecimalPlaces = 1;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
