package io.github.brainage04.brainagehud.util;

import java.time.Duration;
import net.minecraft.client.Minecraft;

public class TimerUtils {
    public static long GPU_USAGE = 0;
    public static long GPU_LAST_UPDATED = System.currentTimeMillis();

    public static long CPU_USAGE = 0;
    public static long CPU_LAST_UPDATED = System.currentTimeMillis();
    private static long cpuTimeNanos = getProcessCpuTimeNanos();

    public static volatile long ping = 0;
    public static volatile double tps = 0;

    public static void updateGpuUsage(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - GPU_LAST_UPDATED <= millisecondsBetweenUpdates) return;

        GPU_USAGE = (long) Minecraft.getInstance().getGpuUtilization();
        GPU_LAST_UPDATED = now;
    }

    public static void updateCpuUsage(int millisecondsBetweenUpdates) {
        long now = System.currentTimeMillis();
        if (now - CPU_LAST_UPDATED <= millisecondsBetweenUpdates) return;

        long currentCpuTimeNanos = getProcessCpuTimeNanos();
        long elapsedNanos = (now - CPU_LAST_UPDATED) * 1_000_000L;
        if (elapsedNanos > 0L) {
            CPU_USAGE = (currentCpuTimeNanos - cpuTimeNanos) * 100L / (elapsedNanos * Runtime.getRuntime().availableProcessors());
        }
        cpuTimeNanos = currentCpuTimeNanos;
        CPU_LAST_UPDATED = now;
    }

    private static long getProcessCpuTimeNanos() {
        return ProcessHandle.current().info().totalCpuDuration().map(Duration::toNanos).orElse(0L);
    }
}
