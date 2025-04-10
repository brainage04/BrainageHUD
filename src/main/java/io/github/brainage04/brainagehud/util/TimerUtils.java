package io.github.brainage04.brainagehud.util;

import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.MinecraftClient;

import java.lang.management.ManagementFactory;

public class TimerUtils {
    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static long GPU_USAGE = 0;
    public static long GPU_LAST_UPDATED = System.currentTimeMillis();

    public static long CPU_USAGE = 0;
    public static long CPU_LAST_UPDATED = System.currentTimeMillis();

    public static volatile long ping = 0;
    public static volatile double tps = 0;

    public static void updateGpuUsage(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - GPU_LAST_UPDATED <= millisecondsBetweenUpdates) return;

        // todo: fix this not working without DebugHud enabled
        GPU_USAGE = (long) MinecraftClient.getInstance().getGpuUtilizationPercentage();
        GPU_LAST_UPDATED = System.currentTimeMillis();
    }

    public static void updateCpuUsage(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - CPU_LAST_UPDATED <= millisecondsBetweenUpdates) return;

        CPU_USAGE = Math.round(osBean.getProcessCpuLoad() * 100);
        CPU_LAST_UPDATED = System.currentTimeMillis();
    }
}
