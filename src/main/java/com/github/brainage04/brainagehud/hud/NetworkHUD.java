package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.ModConfig;
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
    // todo: separate ping and tps into individual HUD elements?
    public static void networkHud(TextRenderer renderer, DrawContext drawContext, ModConfig.NetworkHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        // works well enough (for now)
        if (settings.showPing) {
            TimerUtils.updatePing(500);

            lines.add(
                    String.format(
                            "Ping: %sms",
                            TimerUtils.AVERAGE_PING
                    )
            );
        }

        // only works on singleplayer? may switch to measuring how many packets have been received from server in last N seconds
        if (settings.showTps) {
            TimerUtils.updateTps(500);

            lines.add(
                    String.format(
                            "TPS: %s",
                            roundDecimalPlaces(TimerUtils.AVERAGE_TPS, settings.tpsDecimalPlaces)
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}