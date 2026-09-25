package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class EntityHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Entity HUD", false, -5, 125, ElementAnchor.TOP_RIGHT);
    @ConfigEntry.Gui.Tooltip public boolean showCreatures = true;
    @ConfigEntry.Gui.Tooltip public boolean showWaterCreatures = true;
    @ConfigEntry.Gui.Tooltip public boolean showAmbient = true;
    @ConfigEntry.Gui.Tooltip public boolean showMonsters = true;
    @ConfigEntry.Gui.Tooltip public boolean showOthers = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
