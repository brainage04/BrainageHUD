package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class StatusEffectHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Status Effect HUD", true, 5, 144, ElementAnchor.TOP_LEFT);
    @ConfigEntry.Gui.Tooltip public boolean showDurations = true;
    @ConfigEntry.Gui.Tooltip public boolean showIcons = true;
    @ConfigEntry.Gui.Tooltip public boolean hideVanillaStatusEffects = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
