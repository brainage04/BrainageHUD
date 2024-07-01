package com.github.brainage04.brainagehud.util;

import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.MinecraftClient;

import java.lang.management.ManagementFactory;

public class TimerUtils {
    public static long GPU_USAGE = 0;
    public static long GPU_LAST_UPDATED = System.currentTimeMillis();

    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    public static long CPU_USAGE = 0;
    public static long CPU_LAST_UPDATED = System.currentTimeMillis();

    public static void updateGpuUsage(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - GPU_LAST_UPDATED > millisecondsBetweenUpdates) {
            GPU_USAGE = (long) MinecraftClient.getInstance().getGpuUtilizationPercentage();
            GPU_LAST_UPDATED = System.currentTimeMillis();
        }
    }

    public static void updateCpuUsage(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - CPU_LAST_UPDATED > millisecondsBetweenUpdates) {
            CPU_USAGE = Math.round(osBean.getProcessCpuLoad() * 100);
            CPU_LAST_UPDATED = System.currentTimeMillis();
        }
    }
}
