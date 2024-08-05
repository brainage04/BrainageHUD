package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;

public class KillDeathRatioHUD {
    public static void killDeathRatioHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.KillDeathRatioHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        switch (settings.killDeathRatioFormat) {
            case BOTH_NUMBERS -> lines.add(
                    String.format(
                            "KDR: %d / %d",
                            settings.kills,
                            settings.deaths
                    )
            );
            case SIMPLIFIED -> lines.add(
                    String.format(
                            "KDR: %f",
                            ((float) settings.kills) / settings.deaths
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}
