package io.github.brainage04.brainagehud.event;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.util.RollingAverage;
import io.github.brainage04.brainagehud.util.TickRateTracker;

public class ModPacketEvents {
    private static final RollingAverage PING = new RollingAverage();
    private static final TickRateTracker TICK_RATE = new TickRateTracker(System::nanoTime);

    public static void onLogin() {
        PING.clear();
        TICK_RATE.reset();
    }

    /** Called on the network thread with the round trip time of a ping. */
    public static void onPong(long roundTripMillis) {
        PING.add(roundTripMillis, getConfig().networkHudConfig.pingIntervalsTracked);
    }

    /** Called on the render thread with the game time of a report from {@code world}. */
    public static void onServerGameTime(long gameTime, Object world) {
        TICK_RATE.record(gameTime, getConfig().networkHudConfig.tpsIntervalsTracked, world);
    }

    /**
     * Called when the server changes its tick rate or freezes: the intervals measured before that
     * describe a different server, so they are dropped.
     */
    public static void onTickRateStateChanged() {
        TICK_RATE.reset();
    }

    public static long getPing() {
        return PING.mean();
    }

    public static TickRateTracker tickRateTracker() {
        return TICK_RATE;
    }
}
