package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PerformanceHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showFps;
    public boolean showRamUsage;
    public boolean showCpuUsage;
    @ConfigEntry.Gui.Tooltip(count = 3)
    public boolean showGpuUsage;

    public PerformanceHudConfig() {
        this.coreSettings = new CoreSettings(4, "Performance HUD", true, 5, 77, ElementAnchor.TOP_LEFT);
        this.showFps = true;
        this.showRamUsage = false;
        this.showCpuUsage = false;
        this.showGpuUsage = false;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
