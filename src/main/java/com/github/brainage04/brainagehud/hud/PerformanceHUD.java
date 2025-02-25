package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.ModConfig;
import com.github.brainage04.brainagehud.util.TimerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;

public class PerformanceHUD {
    public static void performanceHud(TextRenderer renderer, DrawContext drawContext, ModConfig.PerformanceHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        if (settings.showFps) {
            lines.add(
                    String.format(
                            "%d FPS",
                            MinecraftClient.getInstance().getCurrentFps()
                    )
            );
        }

        // taken from net.minecraft.client.gui.hud.DebugHud
        if (settings.showRamUsage) {
            long l = Runtime.getRuntime().maxMemory();
            long m = Runtime.getRuntime().totalMemory();
            long n = Runtime.getRuntime().freeMemory();
            long o = m - n;

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "RAM: %d%% (%d/%dMB)",
                            o * 100L / l,
                            o / 1_048_576L,
                            l / 1_048_576L
                    )
            );
        }

        if (settings.showGpuUsage) {
            TimerUtils.updateGpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "GPU: %d%%",
                            TimerUtils.GPU_USAGE
                    )
            );
        }

        if (settings.showCpuUsage) {
            TimerUtils.updateCpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "CPU: %d%%",
                            TimerUtils.CPU_USAGE
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}