package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ReachHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings(6, "Reach HUD", true, 0, 30, ElementAnchor.CENTER);
    public int decimalPlaces = 2;
    public boolean showName = true;
    public boolean showCoordinates = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
