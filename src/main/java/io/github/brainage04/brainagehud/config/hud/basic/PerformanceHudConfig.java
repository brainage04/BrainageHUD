package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PerformanceHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings(4, "Performance HUD", true, 5, 99, ElementAnchor.TOP_LEFT);
    public boolean showFps = true;
    public boolean showRamUsage = false;
    public boolean showCpuUsage = false;
    @ConfigEntry.Gui.Tooltip(count = 3) public boolean showGpuUsage = false;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
