package com.github.brainage04.brainagehud.config.hud.basic;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;

@SuppressWarnings("CanBeFinal")
public class DateTimeHudConfig extends CoreSettingsContainer {
    public boolean showDate;
    public boolean showTime;
    public boolean twelveHourFormat;
    public boolean showTimezone;

    public DateTimeHudConfig() {
        this.coreSettings = new CoreSettings(2, "Date Time HUD", true, -77, 5, ElementAnchor.TOP_RIGHT, false, 100);
        this.showDate = true;
        this.showTime = true;
        this.twelveHourFormat = true;
        this.showTimezone = true;
    }
}
