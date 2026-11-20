package io.github.brainage04.brainagehud.util;

import java.util.ArrayDeque;
import java.util.function.LongSupplier;

/**
 * Estimates the server's ticks per second from the game time it reports.
 *
 * <p>The server sends its game time roughly once a second. Each report is compared with the
 * previous one: the game ticks that elapsed divided by the wall-clock time that elapsed is the rate
 * at which the server is actually ticking. Summing both over a window, rather than averaging
 * per-interval rates, keeps irregularly spaced reports correctly weighted.
 */
public final class TickRateTracker {
    /** Vanilla broadcasts the game time every 20 ticks (MinecraftServer.java:1079). */
    private static final int VANILLA_REPORT_INTERVAL_TICKS = 20;

    /** Three report intervals may pass with no report before the rate counts as unknown. */
    private static final int STALE_AFTER_INTERVALS = 3;

    private record Interval(long ticks, long nanos) {}

    private final LongSupplier nanoClock;
    private final ArrayDeque<Interval> intervals = new ArrayDeque<>();
    private long totalTicks;
    private long totalNanos;
    private boolean hasPrevious;
    private long previousGameTime;
    private long previousNanos;

    public TickRateTracker(LongSupplier nanoClock) {
        this.nanoClock = nanoClock;
    }

    /**
     * Records a game time report, keeping at most {@code maxIntervals} intervals. A game time that
     * goes backwards means the server's clock restarted, so the history restarts.
     */
    public void record(long gameTime, int maxIntervals) {
        long nowNanos = nanoClock.getAsLong();

        if (hasPrevious) {
            long ticks = gameTime - previousGameTime;
            long nanos = nowNanos - previousNanos;

            if (ticks < 0L || nanos <= 0L) {
                clearIntervals();
            } else if (ticks > 0L) {
                // a duplicate report measures no ticks, so it would dilute the window: one after a
                // healthy second would read 10 TPS
                intervals.addLast(new Interval(ticks, nanos));
                totalTicks += ticks;
                totalNanos += nanos;
            }
        }

        int limit = Math.max(1, maxIntervals);
        while (intervals.size() > limit) {
            Interval oldest = intervals.removeFirst();
            totalTicks -= oldest.ticks();
            totalNanos -= oldest.nanos();
        }

        hasPrevious = true;
        previousGameTime = gameTime;
        previousNanos = nowNanos;
    }

    /**
     * Whether the reports measure a rate: at least one tick has elapsed, and the newest report is
     * recent enough. A server that stops sending the game time would otherwise keep showing the
     * last interval it sent, and two reports with the same game time would read as 0.0 TPS.
     *
     * @param tickRate the server's target tick rate, at least 1
     */
    public boolean hasRate(float tickRate) {
        if (totalTicks <= 0L || totalNanos <= 0L) return false;
        return nanoClock.getAsLong() - previousNanos <= staleAfterNanos(tickRate);
    }

    public double ticksPerSecond() {
        return totalNanos > 0L ? totalTicks * 1_000_000_000.0D / totalNanos : 0.0D;
    }

    public void reset() {
        clearIntervals();
        hasPrevious = false;
    }

    /**
     * Three report intervals, or three of the mean interval measured so far when that is longer: a
     * server that is lagging sends its reports further apart, and its rate is still measurable
     * between them.
     */
    private long staleAfterNanos(float tickRate) {
        // TickRateManager.MIN_TICKRATE is 1.0F, so a real tick rate is never below 1
        long vanillaInterval =
                (long) (VANILLA_REPORT_INTERVAL_TICKS * 1_000_000_000.0D / Math.max(1.0F, tickRate));
        long meanInterval = totalNanos / intervals.size();
        return STALE_AFTER_INTERVALS * Math.max(vanillaInterval, meanInterval);
    }

    private void clearIntervals() {
        intervals.clear();
        totalTicks = 0L;
        totalNanos = 0L;
    }
}
