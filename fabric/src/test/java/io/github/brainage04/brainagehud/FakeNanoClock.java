package io.github.brainage04.brainagehud;

import java.util.function.LongSupplier;

/** A clock the test moves by hand, so a tracker's timing is deterministic. */
public final class FakeNanoClock implements LongSupplier {
    private long now;

    public void set(long nowNanos) {
        this.now = nowNanos;
    }

    @Override
    public long getAsLong() {
        return now;
    }
}
