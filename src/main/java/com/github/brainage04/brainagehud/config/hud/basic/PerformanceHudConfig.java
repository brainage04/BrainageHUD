package com.github.brainage04.brainagehud.config.hud.basic;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PerformanceHudConfig extends CoreSettingsContainer {
    public boolean showFps;
    public boolean showRamUsage;
    public boolean showCpuUsage;
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean showGpuUsage;

    public PerformanceHudConfig() {
        this.coreSettings = new CoreSettings(4, "Performance HUD", true, 5, 77, ElementAnchor.TOP_LEFT, false, 100);
        this.showFps = true;
        this.showRamUsage = false;
        this.showCpuUsage = false;
        this.showGpuUsage = false;
    }
}
