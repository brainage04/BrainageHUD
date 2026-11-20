package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.brainage04.brainagehud.FakeNanoClock;
import org.junit.jupiter.api.Test;

class TickRateTrackerTest {
    private static final long SECOND = 1_000_000_000L;
    private static final float VANILLA_TICK_RATE = 20.0F;

    private final FakeNanoClock clock = new FakeNanoClock();
    private final TickRateTracker tracker = new TickRateTracker(clock);

    @Test
    void needsTwoReportsBeforeItHasARate() {
        tracker.record(1000L, 3);

        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));
    }

    @Test
    void measuresHowFastTheServerActuallyTicks() {
        for (int report = 0; report <= 3; report++) {
            clock.set(report * 2L * SECOND);
            tracker.record(1000L + report * 20L, 3);
        }

        assertEquals(10.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void weightsIrregularReportsByElapsedTime() {
        tracker.record(0L, 5);
        clock.set(SECOND);
        tracker.record(20L, 5);
        // a lagging half second after a full-speed second: averaging the two rates (20 and 10)
        // would give 15, but 25 ticks ran in 1.5 seconds
        clock.set(SECOND + SECOND / 2);
        tracker.record(25L, 5);

        assertEquals(25.0D / 1.5D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void forgetsOlderIntervalsWhenTheWindowShrinks() {
        for (int report = 0; report <= 10; report++) {
            clock.set(report * 2L * SECOND);
            tracker.record(report * 20L, 30);
        }
        clock.set(21L * SECOND);
        tracker.record(220L, 2);
        clock.set(22L * SECOND);
        tracker.record(240L, 2);

        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void restartsWhenGameTimeGoesBackwards() {
        tracker.record(100_000L, 3);
        clock.set(SECOND);
        tracker.record(100_010L, 3);

        clock.set(2L * SECOND);
        tracker.record(500L, 3);
        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));

        clock.set(3L * SECOND);
        tracker.record(520L, 3);
        assertTrue(tracker.hasRate(VANILLA_TICK_RATE));
        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void aRespawnKeepsTheRate() {
        tracker.record(100_000L, 3);
        clock.set(SECOND);
        tracker.record(100_020L, 3);
        tracker.onTickRateState(VANILLA_TICK_RATE, false);

        // a respawn sends the level info: a set-time packet, then the same ticking state
        // (PlayerList.java:658, :667)
        clock.set(2L * SECOND);
        tracker.record(100_040L, 3);
        tracker.onTickRateState(VANILLA_TICK_RATE, false);

        assertTrue(tracker.hasRate(VANILLA_TICK_RATE));
        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void aChangedTickRateDropsTheIntervals() {
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);
        tracker.onTickRateState(VANILLA_TICK_RATE, false);

        // /tick rate 10: the intervals measured at 20 TPS describe a different server
        tracker.onTickRateState(10.0F, false);

        assertFalse(tracker.hasRate(10.0F));
    }

    @Test
    void freezingDropsTheIntervals() {
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);
        tracker.onTickRateState(VANILLA_TICK_RATE, false);

        tracker.onTickRateState(VANILLA_TICK_RATE, true);

        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));
    }

    @Test
    void resetDiscardsThePreviousReport() {
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);

        tracker.reset();
        clock.set(2L * SECOND);
        tracker.record(1_000_000L, 3);

        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));
    }

    @Test
    void twoReportsWithTheSameGameTimeAreNotARate() {
        tracker.record(1000L, 3);
        clock.set(SECOND);
        tracker.record(1000L, 3);

        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));
    }

    @Test
    void aDuplicateReportDoesNotDiluteTheRate() {
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);

        // one report with no ticks in it, then the server carries on at 20 TPS
        clock.set(2L * SECOND);
        tracker.record(20L, 3);
        clock.set(3L * SECOND);
        tracker.record(40L, 3);

        assertEquals(20.0D, tracker.ticksPerSecond(), 1e-9);
    }

    @Test
    void rateGoesUnknownOnceReportsStopArriving() {
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);
        assertTrue(tracker.hasRate(VANILLA_TICK_RATE));

        // vanilla reports every 20 ticks, so one second at 20 TPS; three of those may pass with no
        // report before the rate is unknown
        clock.set(SECOND + 3L * SECOND);
        assertTrue(tracker.hasRate(VANILLA_TICK_RATE));

        clock.set(SECOND + 3L * SECOND + 1L);
        assertFalse(tracker.hasRate(VANILLA_TICK_RATE));
    }

    @Test
    void aLaggingServerKeepsItsRateBetweenSlowReports() {
        tracker.record(0L, 3);
        // 20 ticks in 4 seconds: 5 TPS, so the reports themselves are 4 seconds apart
        clock.set(4L * SECOND);
        tracker.record(20L, 3);

        clock.set(4L * SECOND + 3L * SECOND);
        assertTrue(tracker.hasRate(VANILLA_TICK_RATE));
        assertEquals(5.0D, tracker.ticksPerSecond(), 1e-9);
    }
}
