package com.github.brainage04.brainagehud.config.hud;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class DateTimeHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showDate;
    public boolean showTime;
    public boolean twelveHourFormat;
    public boolean showTimezone;

    public DateTimeHUDConfig() {
        this.coreSettings = new CoreSettings(2, "Date Time HUD", true, -85, 5, ElementAnchor.TOP_RIGHT, false, 100);
        this.showDate = true;
        this.showTime = true;
        this.twelveHourFormat = true;
        this.showTimezone = true;
    }
}
