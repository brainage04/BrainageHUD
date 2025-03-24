package com.github.brainage04.brainagehud.config.hud;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ReachHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public int decimalPlaces;
    public boolean showName;
    public boolean onlyUpdateOnLeftClick;

    public ReachHUDConfig() {
        this.coreSettings = new CoreSettings(6, "Reach HUD", true, 0, 20, ElementAnchor.CENTER, false, 100);
        this.decimalPlaces = 2;
        this.showName = true;
        this.onlyUpdateOnLeftClick = false;
    }
}
