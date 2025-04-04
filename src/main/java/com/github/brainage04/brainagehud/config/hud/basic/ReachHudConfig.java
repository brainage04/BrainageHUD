package com.github.brainage04.brainagehud.config.hud.basic;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;

@SuppressWarnings("CanBeFinal")
public class ReachHudConfig extends CoreSettingsContainer {
    public int decimalPlaces;
    public boolean showName;

    public ReachHudConfig() {
        this.coreSettings = new CoreSettings(6, "Reach HUD", true, 0, 30, ElementAnchor.CENTER, false, 100);
        this.decimalPlaces = 2;
        this.showName = true;
    }
}
