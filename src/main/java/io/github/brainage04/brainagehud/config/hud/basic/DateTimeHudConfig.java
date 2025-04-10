package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class DateTimeHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showDate;
    public boolean showTime;
    public boolean twelveHourFormat;
    public boolean showTimezone;

    public DateTimeHudConfig() {
        this.coreSettings = new CoreSettings(2, "Date Time HUD", true, -77, 5, ElementAnchor.TOP_RIGHT);
        this.showDate = true;
        this.showTime = true;
        this.twelveHourFormat = true;
        this.showTimezone = true;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
