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

    /** Called on the render thread with the game time of a server report. */
    public static void onServerGameTime(long gameTime) {
        TICK_RATE.record(gameTime, getConfig().networkHudConfig.tpsIntervalsTracked);
    }

    /**
     * Called with the server's ticking state. The intervals measured before a change describe a
     * different server, so they are dropped; a respawn re-sends the same state and keeps them.
     */
    public static void onTickRateState(float tickRate, boolean frozen) {
        TICK_RATE.onTickRateState(tickRate, frozen);
    }

    public static long getPing() {
        return PING.mean();
    }

    public static TickRateTracker tickRateTracker() {
        return TICK_RATE;
    }
}
