package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class DateTimeHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings(2, "Date Time HUD", true, -77, 5, ElementAnchor.TOP_RIGHT);
    public boolean showDate = true;
    public boolean showTime = true;
    public boolean twelveHourFormat = true;
    public boolean showTimezone = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
