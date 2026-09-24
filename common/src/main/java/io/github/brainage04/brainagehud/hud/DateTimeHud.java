package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.DateTimeHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class DateTimeHud implements BasicCoreHudElement<DateTimeHudConfig> {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("E dd MMM yyyy");
    private static final DateTimeFormatter TIME_12_HOUR = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private static final DateTimeFormatter TIME_24_HOUR = DateTimeFormatter.ofPattern("HH:mm:ss");
    // "xxx" always prints an offset such as +00:00, whereas "XXX" prints "Z" for UTC
    private static final DateTimeFormatter TIMEZONE = DateTimeFormatter.ofPattern("z '(UTC 'xxx')'");

    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        DateTimeHudConfig config = getElementConfig();
        // one timestamp for every line, so the date and time cannot straddle midnight
        ZonedDateTime now = ZonedDateTime.now();

        if (config.showDate) {
            lines.add(DATE.format(now));
        }

        if (config.showTime) {
            if (config.twelveHourFormat) {
                lines.add(
                        TIME_12_HOUR.format(now)
                                .replace("am", "AM")
                                .replace("pm", "PM")
                );
            } else {
                lines.add(TIME_24_HOUR.format(now));
            }
        }

        if (config.showTimezone) {
            lines.add(TIMEZONE.format(now));
        }

        return lines;
    }

    @Override
    public DateTimeHudConfig getElementConfig() {
        return getConfig().dateTimeHudConfig;
    }
}
