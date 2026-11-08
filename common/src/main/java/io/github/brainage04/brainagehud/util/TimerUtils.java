package io.github.brainage04.brainagehud.util;

import io.github.brainage04.brainagehud.mixin.MixinMinecraft;
import java.time.Duration;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;

public class TimerUtils {
    private static double gpuUsage = 0;
    private static long gpuTimeNanos = 0;
    private static long gpuLastUpdated = System.currentTimeMillis();

    private static long cpuUsage = 0;
    private static long cpuLastUpdated = System.currentTimeMillis();
    private static long cpuTimeNanos = getProcessCpuTimeNanos();

    public static OptionalLong getGpuUsage(int millisecondsBetweenUpdates) {
        updateGpu(millisecondsBetweenUpdates);
        return gpuTimeNanos == 0 ? OptionalLong.empty() : OptionalLong.of((long) gpuUsage);
    }

    public static OptionalDouble getGpuFrameTimeMillis(int millisecondsBetweenUpdates) {
        updateGpu(millisecondsBetweenUpdates);
        return gpuTimeNanos == 0
                ? OptionalDouble.empty()
                : OptionalDouble.of(gpuTimeNanos / 1_000_000.0D);
    }

    /**
     * Reads the timer query behind the F3 GPU utilization, which the game divides by the frame time
     * to get that utilization. It averages the GPU time of the last few frames, and reads 0 until a
     * frame has been measured.
     */
    private static void updateGpu(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - gpuLastUpdated > millisecondsBetweenUpdates) {
            Minecraft minecraft = Minecraft.getInstance();
            gpuUsage = minecraft.getGpuUtilization();
            gpuTimeNanos = ((MixinMinecraft) minecraft).brainagehud$getTimerQuery().get();
            gpuLastUpdated = now;
        }
    }

    public static long getCpuUsage(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - cpuLastUpdated > millisecondsBetweenUpdates) {
            long currentCpuTimeNanos = getProcessCpuTimeNanos();
            long elapsedNanos = (now - cpuLastUpdated) * 1_000_000L;
            cpuUsage =
                    (currentCpuTimeNanos - cpuTimeNanos)
                            * 100L
                            / (elapsedNanos * Runtime.getRuntime().availableProcessors());
            cpuTimeNanos = currentCpuTimeNanos;
            cpuLastUpdated = now;
        }

        return cpuUsage;
    }

    private static long getProcessCpuTimeNanos() {
        return ProcessHandle.current().info().totalCpuDuration().map(Duration::toNanos).orElse(0L);
    }
}
