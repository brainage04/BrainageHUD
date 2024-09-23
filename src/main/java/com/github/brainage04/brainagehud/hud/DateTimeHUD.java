package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;

public class DateTimeHUD {
    public static void dateTimeHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.DateTimeHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        if (settings.showDate) {
            lines.add(new SimpleDateFormat("E dd MMM yyyy").format(new Date()));
        }

        if (settings.showTime) {
            if (settings.twelveHourFormat) {
                lines.add(
                        new SimpleDateFormat("hh:mm:ss a").format(new Date())
                        .replace("am", "AM")
                        .replace("pm", "PM")
                );
            } else {
                lines.add(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
        }

        if (settings.showTimezone) {
            lines.add(
                    String.format(
                            "%s (UTC %s)",
                            new SimpleDateFormat("z").format(new Date()),
                            new SimpleDateFormat("XXX").format(new Date())
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}