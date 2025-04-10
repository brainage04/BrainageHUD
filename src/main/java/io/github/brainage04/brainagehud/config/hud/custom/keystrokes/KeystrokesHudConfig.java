package io.github.brainage04.brainagehud.config.hud.custom.keystrokes;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class KeystrokesHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public int keyBackdropOpacity;
    public boolean showWasd;
    public boolean showSpace;
    public boolean showMouseButtons;
    public ClicksPerSecondFormat clicksPerSecondFormat;

    public KeystrokesHudConfig() {
        this.coreSettings = new CoreSettings(1, "Keystrokes HUD", true, -5, 5, ElementAnchor.TOP_RIGHT);
        this.keyBackdropOpacity = 100;
        this.showWasd = true;
        this.showSpace = true;
        this.showMouseButtons = true;
        this.clicksPerSecondFormat = ClicksPerSecondFormat.LEFT_CLICK;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
