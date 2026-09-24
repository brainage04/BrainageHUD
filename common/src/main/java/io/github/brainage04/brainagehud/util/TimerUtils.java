package io.github.brainage04.brainagehud.util;

import java.time.Duration;
import net.minecraft.client.Minecraft;

/** Throttled GPU and CPU usage readings. Used only on the render thread. */
public class TimerUtils {
    private static long gpuUsage = 0;
    private static long gpuLastUpdated = System.currentTimeMillis();

    private static long cpuUsage = 0;
    private static long cpuLastUpdated = System.currentTimeMillis();
    private static long cpuTimeNanos = getProcessCpuTimeNanos();

    /** The GPU usage percentage, refreshed at most once per {@code millisecondsBetweenUpdates}. */
    public static long getGpuUsage(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - gpuLastUpdated > millisecondsBetweenUpdates) {
            gpuUsage = (long) Minecraft.getInstance().getGpuUtilization();
            gpuLastUpdated = now;
        }

        return gpuUsage;
    }

    /** The process CPU usage percentage, refreshed at most once per {@code millisecondsBetweenUpdates}. */
    public static long getCpuUsage(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - cpuLastUpdated > millisecondsBetweenUpdates) {
            long currentCpuTimeNanos = getProcessCpuTimeNanos();
            long elapsedNanos = (now - cpuLastUpdated) * 1_000_000L;
            cpuUsage = (currentCpuTimeNanos - cpuTimeNanos) * 100L / (elapsedNanos * Runtime.getRuntime().availableProcessors());
            cpuTimeNanos = currentCpuTimeNanos;
            cpuLastUpdated = now;
        }

        return cpuUsage;
    }

    private static long getProcessCpuTimeNanos() {
        return ProcessHandle.current().info().totalCpuDuration().map(Duration::toNanos).orElse(0L);
    }
}
