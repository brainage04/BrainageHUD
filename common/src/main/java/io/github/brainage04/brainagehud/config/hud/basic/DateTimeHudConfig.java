package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class DateTimeHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Date/Time HUD", true, -77, 5, ElementAnchor.TOP_RIGHT);
    @ConfigEntry.Gui.Tooltip public boolean showDate = true;
    public boolean showTime = true;
    @ConfigEntry.Gui.Tooltip public boolean twelveHourFormat = true;
    @ConfigEntry.Gui.Tooltip public boolean showTimezone = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
