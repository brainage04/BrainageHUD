package com.github.brainage04.brainagehud.config.hud.basic;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;

@SuppressWarnings("CanBeFinal")
public class ToggleSprintHudConfig extends CoreSettingsContainer {
    public ToggleSprintHudConfig() {
        this.coreSettings = new CoreSettings(7, "Toggle Sprint HUD", true, 5, -5, ElementAnchor.BOTTOM_LEFT, false, 100);
    }
}
