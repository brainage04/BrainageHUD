package com.github.brainage04.brainagehud.config.hud.custom.keystrokes;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;

@SuppressWarnings("CanBeFinal")
public class KeystrokesHudConfig extends CoreSettingsContainer {
    public int keyBackdropOpacity;
    public boolean showWasd;
    public boolean showSpace;
    public boolean showMouseButtons;
    public ClicksPerSecondFormat clicksPerSecondFormat;

    public KeystrokesHudConfig() {
        this.coreSettings = new CoreSettings(1, "Keystrokes HUD", true, -5, 5, ElementAnchor.TOP_RIGHT, true, 0);
        this.keyBackdropOpacity = 100;
        this.showWasd = true;
        this.showSpace = true;
        this.showMouseButtons = true;
        this.clicksPerSecondFormat = ClicksPerSecondFormat.LEFT_CLICK;
    }
}
