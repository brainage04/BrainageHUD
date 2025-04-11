package io.github.brainage04.brainagehud.config.hud.custom.keystrokes;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class KeystrokesHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings(1, "Keystrokes HUD", true, -5, 5, ElementAnchor.TOP_RIGHT);
    public int keyBackdropOpacity = 100;
    public boolean showWasd = true;
    public boolean showSpace = true;
    public boolean showMouseButtons = true;
    public ClicksPerSecondFormat clicksPerSecondFormat = ClicksPerSecondFormat.LEFT_CLICK;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
