package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.basic.DateTimeHudConfig;
import com.github.brainage04.brainagehud.hud.core.BasicHudElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class DateTimeHud implements BasicHudElement<DateTimeHudConfig> {
    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>(List.of());

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
            lines.add(
                    String.format(
                            "%s (UTC %s)",
                            new SimpleDateFormat("z").format(new Date()),
                            new SimpleDateFormat("XXX").format(new Date())
                    )
            );
        }

        return lines;
    }

    @Override
    public DateTimeHudConfig getElementConfig() {
        return getConfig().dateTimeHudConfig;
    }
}