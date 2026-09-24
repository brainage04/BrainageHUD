package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TickRateTrackerTest {
    private static final long SECOND = 1_000_000_000L;

    @Test
    void needsTwoReportsBeforeItHasARate() {
        TickRateTracker tracker = new TickRateTracker();

        tracker.record(1000L, 0L, 3);

        assertFalse(tracker.hasRate());
    }

    @Test
    void measuresHowFastTheServerActuallyTicks() {
        TickRateTracker tracker = new TickRateTracker();

        // a lagging server takes two seconds to run each batch of 20 ticks
        for (int report = 0; report <= 3; report++) {
            tracker.record(1000L + report * 20L, report * 2L * SECOND, 3);
        }

        assertEquals(10.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void weightsIrregularReportsByElapsedTime() {
        TickRateTracker tracker = new TickRateTracker();

        tracker.record(0L, 0L, 5);
        tracker.record(20L, SECOND, 5);
        // a lagging half second after a full-speed second: averaging the two rates (20 and 10)
        // would give 15, but 25 ticks ran in 1.5 seconds
        tracker.record(25L, SECOND + SECOND / 2, 5);

        assertEquals(25.0D / 1.5D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void forgetsOlderIntervalsWhenTheWindowShrinks() {
        TickRateTracker tracker = new TickRateTracker();

        // ten slow intervals, then two healthy ones with the window lowered to two intervals
        for (int report = 0; report <= 10; report++) {
            tracker.record(report * 20L, report * 2L * SECOND, 30);
        }
        tracker.record(220L, 21L * SECOND, 2);
        tracker.record(240L, 22L * SECOND, 2);

        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void restartsWhenGameTimeGoesBackwards() {
        TickRateTracker tracker = new TickRateTracker();
        tracker.record(100_000L, 0L, 3);
        tracker.record(100_010L, SECOND, 3);

        // joining another world whose clock is behind the previous one
        tracker.record(500L, 2L * SECOND, 3);
        assertFalse(tracker.hasRate());

        tracker.record(520L, 3L * SECOND, 3);
        assertTrue(tracker.hasRate());
        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void resetDiscardsThePreviousReport() {
        TickRateTracker tracker = new TickRateTracker();
        tracker.record(0L, 0L, 3);
        tracker.record(20L, SECOND, 3);

        tracker.reset();
        // a new world far ahead in game time must not be compared with the old one
        tracker.record(1_000_000L, 2L * SECOND, 3);

        assertFalse(tracker.hasRate());
    }
}
