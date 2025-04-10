package io.github.brainage04.brainagehud.event;

import io.github.brainage04.brainagehud.util.TimerUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ModPacketEvents {
    private static boolean packetReceivedThisTick = false;
    private static int packetsThisSecond = 0;
    private static final List<Integer> tpsList = new ArrayList<>();
    public static final List<Long> pingList = new ArrayList<>();

    public static void initialize() {
        ClientTickEvents.START_CLIENT_TICK.register(world -> {
            if (!getConfig().networkHudConfig.showTps) return;

            if (packetReceivedThisTick) {
                packetReceivedThisTick = false;
                packetsThisSecond++;
            }

            if (ModTickEvents.getTicks() % getConfig().networkHudConfig.updateTpsTickInterval == 0) {
                if (tpsList.size() >= getConfig().networkHudConfig.tpsIntervalsTracked) tpsList.removeFirst();
                tpsList.add(packetsThisSecond);
                packetsThisSecond = 0;
                TimerUtils.tps = tpsList.stream().mapToInt(Integer::intValue).average().orElse(20.0) * 20.0 / getConfig().networkHudConfig.updateTpsTickInterval;
            }
        });
    }

    public static void onPacket() {
        packetReceivedThisTick = true;
    }
}