package com.github.brainage04.brainagehud.config.hud;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ToggleSprintHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;

    public ToggleSprintHUDConfig() {
        this.coreSettings = new CoreSettings(7, "Toggle Sprint HUD", true, 5, -5, ElementAnchor.BOTTOM_LEFT, false, 100);
    }
}
