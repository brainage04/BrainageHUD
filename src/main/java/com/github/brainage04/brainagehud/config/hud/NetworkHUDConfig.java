package com.github.brainage04.brainagehud.config.hud;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class NetworkHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showPing;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20) public int updatePingTickInterval;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int pingIntervalsTracked;
    public boolean showTps;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20) public int updateTpsTickInterval;
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30) public int tpsIntervalsTracked;
    public int tpsDecimalPlaces;

    public NetworkHUDConfig() {
        this.coreSettings = new CoreSettings(3, "Network HUD", true, 5, 94, ElementAnchor.TOP_LEFT, false, 100);
        this.showPing = true;
        this.updatePingTickInterval = 10;
        this.pingIntervalsTracked = 3;
        this.showTps = true;
        this.updateTpsTickInterval = 10;
        this.tpsIntervalsTracked = 3;
        this.tpsDecimalPlaces = 1;
    }
}
