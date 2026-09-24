package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RollingAverageTest {
    @Test
    void averagesOnlyTheMostRecentSamples() {
        RollingAverage average = new RollingAverage();

        average.add(100L, 2);
        average.add(20L, 2);
        average.add(40L, 2);

        assertEquals(30L, average.mean());
    }

    @Test
    void shrinksImmediatelyWhenTheWindowIsLowered() {
        RollingAverage average = new RollingAverage();
        for (int sample = 0; sample < 30; sample++) {
            average.add(500L, 30);
        }

        average.add(10L, 1);

        assertEquals(10L, average.mean());
    }

    @Test
    void isZeroWithoutSamples() {
        RollingAverage average = new RollingAverage();
        average.add(50L, 3);

        average.clear();

        assertEquals(0L, average.mean());
    }
}
