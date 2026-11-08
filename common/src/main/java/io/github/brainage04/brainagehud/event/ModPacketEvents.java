package io.github.brainage04.brainagehud.event;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.util.RollingAverage;
import io.github.brainage04.brainagehud.util.TickRateTracker;

public class ModPacketEvents {
    private static final RollingAverage PING = new RollingAverage();
    private static final TickRateTracker TICK_RATE = new TickRateTracker();

    public static void onLogin() {
        PING.clear();
        TICK_RATE.reset();
    }

    /** Called on the network thread with the round trip time of a ping. */
    public static void onPong(long roundTripMillis) {
        PING.add(roundTripMillis, getConfig().networkHudConfig.pingIntervalsTracked);
    }

    public static void onServerGameTime(long gameTime) {
        TICK_RATE.record(
                gameTime, System.nanoTime(), getConfig().networkHudConfig.tpsIntervalsTracked);
    }

    public static long getPing() {
        return PING.mean();
    }

    public static boolean hasTps() {
        return TICK_RATE.hasRate();
    }

    public static double getTps() {
        return TICK_RATE.ticksPerSecond();
    }
}
