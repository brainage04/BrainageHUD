package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClickRateTrackerTest {
    @Test
    void countsEveryClickEvenWhenNothingReadsInBetween() {
        ClickRateTracker tracker = new ClickRateTracker();

        for (int click = 0; click < 15; click++) {
            tracker.recordClick(click * 50L);
        }

        assertEquals(15, tracker.clicksPerSecond(700L));
    }

    @Test
    void clicksExpireOneSecondLater() {
        ClickRateTracker tracker = new ClickRateTracker();
        tracker.recordClick(0L);
        tracker.recordClick(500L);

        assertEquals(2, tracker.clicksPerSecond(999L));
        assertEquals(1, tracker.clicksPerSecond(1000L));
        assertEquals(0, tracker.clicksPerSecond(1500L));
    }
}
