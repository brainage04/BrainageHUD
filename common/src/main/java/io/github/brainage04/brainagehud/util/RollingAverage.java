package io.github.brainage04.brainagehud.util;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;

/**
 * The mean of the most recent samples. Methods are synchronized because samples may arrive on the
 * network thread while the render thread reads the mean.
 */
public final class RollingAverage {
    private final LongArrayFIFOQueue samples = new LongArrayFIFOQueue();
    private long sum;

    /** Adds {@code sample}, then drops the oldest samples until at most {@code maxSamples} remain. */
    public synchronized void add(long sample, int maxSamples) {
        samples.enqueue(sample);
        sum += sample;

        int limit = Math.max(1, maxSamples);
        while (samples.size() > limit) {
            sum -= samples.dequeueLong();
        }
    }

    /** The rounded mean of the retained samples, or 0 when there are none. */
    public synchronized long mean() {
        return samples.isEmpty() ? 0L : Math.round((double) sum / samples.size());
    }

    public synchronized void clear() {
        samples.clear();
        sum = 0L;
    }
}
