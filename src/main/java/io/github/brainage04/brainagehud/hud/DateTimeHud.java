package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.DateTimeHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;

import java.text.SimpleDateFormat;
import java.util.Date;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class DateTimeHud implements BasicHudElement<DateTimeHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        if (getElementConfig().showDate) {
            lines.add(new SimpleDateFormat("E dd MMM yyyy").format(new Date()));
        }

        if (getElementConfig().showTime) {
            if (getElementConfig().twelveHourFormat) {
                lines.add(
                        new SimpleDateFormat("hh:mm:ss a").format(new Date())
                                .replace("am", "AM")
                                .replace("pm", "PM")
                );
            } else {
                lines.add(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
        }

        if (getElementConfig().showTimezone) {
            lines.add("%s (UTC %s)".formatted(
                    new SimpleDateFormat("z").format(new Date()),
                    new SimpleDateFormat("XXX").format(new Date())
            ));
        }

        return lines;
    }

    @Override
    public DateTimeHudConfig getElementConfig() {
        return getConfig().dateTimeHudConfig;
    }
}