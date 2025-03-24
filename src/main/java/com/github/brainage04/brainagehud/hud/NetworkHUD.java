package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.NetworkHUDConfig;
import com.github.brainage04.brainagehud.util.TimerUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;
import static com.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class NetworkHUD {
    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    public static void render(TextRenderer renderer, DrawContext drawContext, NetworkHUDConfig settings) {
        if (!settings.coreSettings.enabled) return;

        List<String> lines = new ArrayList<>(List.of());

        if (settings.showPing) {
            lines.add(
                    String.format(
                            "Ping: %sms",
                            TimerUtils.ping
                    )
            );
        }

        if (settings.showTps) {
            lines.add(
                    String.format(
                            "TPS: %s",
                            roundDecimalPlaces(TimerUtils.tps, settings.tpsDecimalPlaces)
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}