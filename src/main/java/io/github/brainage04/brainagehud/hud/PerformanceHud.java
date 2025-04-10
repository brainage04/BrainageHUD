package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PerformanceHudConfig;
import io.github.brainage04.brainagehud.util.TimerUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class PerformanceHud implements BasicHudElement<PerformanceHudConfig> {
    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>(List.of());

        if (getElementConfig().showFps) {
            lines.add(
                    String.format(
                            "%d FPS",
                            MinecraftClient.getInstance().getCurrentFps()
                    )
            );
        }

        // taken from net.minecraft.client.gui.hud.DebugHud
        if (getElementConfig().showRamUsage) {
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

        if (getElementConfig().showGpuUsage) {
            TimerUtils.updateGpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "GPU: %d%%",
                            TimerUtils.GPU_USAGE
                    )
            );
        }

        if (getElementConfig().showCpuUsage) {
            TimerUtils.updateCpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "CPU: %d%%",
                            TimerUtils.CPU_USAGE
                    )
            );
        }

        return lines;
    }

    @Override
    public PerformanceHudConfig getElementConfig() {
        return getConfig().performanceHudConfig;
    }
}