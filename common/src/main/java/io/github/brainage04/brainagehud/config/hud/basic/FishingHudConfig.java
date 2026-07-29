package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class FishingHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Fishing HUD", false, 0, 60, ElementAnchor.CENTER);

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
