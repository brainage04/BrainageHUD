package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PerformanceHudConfig;
import io.github.brainage04.brainagehud.util.TimerUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class PerformanceHud implements BasicCoreHudElement<PerformanceHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        if (getElementConfig().showFps) {
            lines.add("%d FPS".formatted(MinecraftClient.getInstance().getCurrentFps()));
        }

        // taken from net.minecraft.client.gui.hud.DebugHud
        if (getElementConfig().showRamUsage) {
            long l = Runtime.getRuntime().maxMemory();
            long m = Runtime.getRuntime().totalMemory();
            long n = Runtime.getRuntime().freeMemory();
            long o = m - n;

            lines.add("RAM: %d%% (%d/%dMB)".formatted(
                    o * 100L / l,
                    o / 1_048_576L,
                    l / 1_048_576L
            ));
        }

        if (getElementConfig().showGpuUsage) {
            TimerUtils.updateGpuUsage(500);

            lines.add("GPU: %d%%".formatted(TimerUtils.GPU_USAGE));
        }

        if (getElementConfig().showCpuUsage) {
            TimerUtils.updateCpuUsage(500);

            lines.add("CPU: %d%%".formatted(TimerUtils.CPU_USAGE));
        }

        return lines;
    }

    @Override
    public PerformanceHudConfig getElementConfig() {
        return getConfig().performanceHudConfig;
    }
}