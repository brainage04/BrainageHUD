package io.github.brainage04.brainagehud.util;

import java.util.ArrayDeque;

/**
 * Estimates the server's ticks per second from the game time it reports.
 *
 * <p>The server sends its game time roughly once a second. Each report is compared with the
 * previous one: the game ticks that elapsed divided by the wall-clock time that elapsed is the
 * rate at which the server is actually ticking. Summing both over a window, rather than averaging
 * per-interval rates, keeps irregularly spaced reports correctly weighted.
 */
public final class TickRateTracker {
    private record Interval(long ticks, long nanos) {}

    private final ArrayDeque<Interval> intervals = new ArrayDeque<>();
    private long totalTicks;
    private long totalNanos;
    private boolean hasPrevious;
    private long previousGameTime;
    private long previousNanos;

    /**
     * Records a game time report received at {@code nowNanos}, keeping at most {@code maxIntervals}
     * intervals. A game time that goes backwards means a different world, so the history restarts.
     */
    public void record(long gameTime, long nowNanos, int maxIntervals) {
        if (hasPrevious) {
            long ticks = gameTime - previousGameTime;
            long nanos = nowNanos - previousNanos;

            if (ticks < 0L || nanos <= 0L) {
                clearIntervals();
            } else {
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

    /** Whether at least one interval has been measured since the last reset. */
    public boolean hasRate() {
        return totalNanos > 0L;
    }

    /** Game ticks per wall-clock second over the retained intervals, or 0 before any interval. */
    public double ticksPerSecond() {
        return hasRate() ? totalTicks * 1_000_000_000.0D / totalNanos : 0.0D;
    }

    public void reset() {
        clearIntervals();
        hasPrevious = false;
    }

    private void clearIntervals() {
        intervals.clear();
        totalTicks = 0L;
        totalNanos = 0L;
    }
}
