package com.github.brainage04.brainagehud.config.hud.custom;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class KeystrokesHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public int keyBackdropOpacity;
    public boolean showWasd;
    public boolean showSpace;
    public boolean showMouseButtons;
    public ClicksPerSecondFormat clicksPerSecondFormat;

    public KeystrokesHUDConfig() {
        this.coreSettings = new CoreSettings(1, "Keystrokes HUD", true, -5, 5, ElementAnchor.TOP_RIGHT, true, 0);
        this.keyBackdropOpacity = 100;
        this.showWasd = true;
        this.showSpace = true;
        this.showMouseButtons = true;
        this.clicksPerSecondFormat = ClicksPerSecondFormat.LEFT_CLICK;
    }
}
