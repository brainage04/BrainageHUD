package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StatusEffectHudTest {
    @Test
    void formatsDurationsUnderAnHourAsMinutesAndSeconds() {
        assertEquals("0:00", StatusEffectHud.formatDuration(0));
        assertEquals("0:05", StatusEffectHud.formatDuration(5));
        assertEquals("1:23", StatusEffectHud.formatDuration(83));
        assertEquals("59:59", StatusEffectHud.formatDuration(3599));
    }

    @Test
    void formatsDurationsFromAnHourWithHours() {
        assertEquals("1:00:00", StatusEffectHud.formatDuration(3600));
        assertEquals("1:01:05", StatusEffectHud.formatDuration(3665));
        assertEquals("12:00:00", StatusEffectHud.formatDuration(43200));
    }
}
