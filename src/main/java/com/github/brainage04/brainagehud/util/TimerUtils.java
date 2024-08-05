package com.github.brainage04.brainagehud.util;

import com.sun.management.OperatingSystemMXBean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.lang.management.ManagementFactory;
import java.util.List;

public class TimerUtils {
    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public static long GPU_USAGE = 0;
    public static long GPU_LAST_UPDATED = System.currentTimeMillis();

    public static long CPU_USAGE = 0;
    public static long CPU_LAST_UPDATED = System.currentTimeMillis();

    public static long AVERAGE_PING = 0;
    public static long PING_LAST_UPDATED = System.currentTimeMillis();

    public static float AVERAGE_TPS = 0;
    public static long TPS_LAST_UPDATED = System.currentTimeMillis();

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

    public static void updatePing(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - CPU_LAST_UPDATED > millisecondsBetweenUpdates) {
            List<PlayerListEntry> playerList = MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream().toList();
            PlayerListEntry player = null;

            for (PlayerListEntry playerListEntry : playerList) {
                if (playerListEntry.getProfile().getId().equals(MinecraftClient.getInstance().getGameProfile().getId())) {
                    player = playerListEntry;
                }
            }

            if (player != null) {
                AVERAGE_PING = player.getLatency();
            } else {
                AVERAGE_PING = -1;
            }

            PING_LAST_UPDATED = System.currentTimeMillis();
        }
    }

    public static void updateTps(int millisecondsBetweenUpdates) {
        if (System.currentTimeMillis() - TPS_LAST_UPDATED > millisecondsBetweenUpdates) {
            float averageTps = 1000f / MinecraftClient.getInstance().getServer().getAverageTickTime();
            float maxTps = MinecraftClient.getInstance().getServer().getTickManager().getTickRate();
            AVERAGE_TPS = Math.min(averageTps, maxTps);

            TPS_LAST_UPDATED = System.currentTimeMillis();
        }
    }
}
