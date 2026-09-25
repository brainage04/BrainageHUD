package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PerformanceHudConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.brainagehud.util.TimerUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class PerformanceHud implements BasicCoreHudElement<PerformanceHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        if (getElementConfig().showFps) {
            lines.add("%d FPS".formatted(Minecraft.getInstance().getFps()));
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
            OptionalLong usage = TimerUtils.getGpuUsage(500);
            lines.add(usage.isPresent() ? "GPU: %d%%".formatted(usage.getAsLong()) : "GPU: -");
        }

        if (getElementConfig().showGpuFrameTime) {
            OptionalDouble frameTime = TimerUtils.getGpuFrameTimeMillis(500);
            lines.add(frameTime.isPresent() ? "GPU Time: %s ms".formatted(MathUtils.roundDecimalPlaces(frameTime.getAsDouble(), 1)) : "GPU Time: -");
        }

        if (getElementConfig().showCpuUsage) {
            lines.add("CPU: %d%%".formatted(TimerUtils.getCpuUsage(500)));
        }

        return lines;
    }

    /** Whether a GPU line is shown, which needs the game to keep measuring the GPU. */
    public static boolean isGpuProfilingEnabled() {
        PerformanceHudConfig config = getConfig().performanceHudConfig;
        return config.coreSettings.enabled && (config.showGpuUsage || config.showGpuFrameTime);
    }

    @Override
    public PerformanceHudConfig getElementConfig() {
        return getConfig().performanceHudConfig;
    }
}