package io.github.brainage04.brainagehud.util;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

/** Counts the clicks made during the last second. Used only on the render thread. */
public final class ClickRateTracker {
    private static final long WINDOW_MILLIS = 1000L;

    private final LongArrayFIFOQueue clickTimes = new LongArrayFIFOQueue();

    public void recordClick(long nowMillis) {
        clickTimes.enqueue(nowMillis);
        expire(nowMillis);
    }

    public int clicksPerSecond(long nowMillis) {
        expire(nowMillis);
        return clickTimes.size();
    }

    private void expire(long nowMillis) {
        while (!clickTimes.isEmpty() && nowMillis - clickTimes.firstLong() >= WINDOW_MILLIS) {
            clickTimes.dequeueLong();
        }
    }
}
