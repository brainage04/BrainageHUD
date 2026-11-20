package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.FakeNanoClock;
import io.github.brainage04.brainagehud.util.TickRateTracker;
import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;

class NetworkHudTest {
    private static final long SECOND = 1_000_000_000L;
    private static final float VANILLA_TICK_RATE = 20.0F;
    private static final Object OVERWORLD = new Object();

    private final FakeNanoClock clock = new FakeNanoClock();
    private final TickRateTracker tracker = new TickRateTracker(clock);

    @Test
    void pingColourChangesAtEachLatencyThreshold() {
        assertEquals(ChatFormatting.DARK_GREEN, NetworkHud.pingColour(49));
        assertEquals(ChatFormatting.GREEN, NetworkHud.pingColour(50));
        assertEquals(ChatFormatting.GREEN, NetworkHud.pingColour(99));
        assertEquals(ChatFormatting.YELLOW, NetworkHud.pingColour(100));
        assertEquals(ChatFormatting.YELLOW, NetworkHud.pingColour(199));
        assertEquals(ChatFormatting.RED, NetworkHud.pingColour(200));
        assertEquals(ChatFormatting.RED, NetworkHud.pingColour(299));
        assertEquals(ChatFormatting.DARK_RED, NetworkHud.pingColour(300));
    }

    @Test
    void tpsColourDistinguishesHealthyAndLaggingRates() {
        assertEquals(ChatFormatting.DARK_GREEN, NetworkHud.tpsColour(20));
        assertEquals(ChatFormatting.GREEN, NetworkHud.tpsColour(19));
        assertEquals(ChatFormatting.GOLD, NetworkHud.tpsColour(18));
        assertEquals(ChatFormatting.RED, NetworkHud.tpsColour(15));
        assertEquals(ChatFormatting.DARK_RED, NetworkHud.tpsColour(10));
    }

    @Test
    void showsTheMeasuredRateWhileReportsKeepArriving() {
        tracker.record(0L, 3, OVERWORLD);
        clock.set(SECOND);
        tracker.record(20L, 3, OVERWORLD);

        assertEquals("TPS: 20.0", tpsLine(false));
    }

    @Test
    void reportsNoRateWhenTheServerStopsSendingGameTime() {
        // Minemen sends two game time reports with the same game time, then none at all. The
        // second report leaves a zero-tick interval, which reads as 0.0 TPS for as long as the
        // client stays connected.
        tracker.record(1000L, 3, OVERWORLD);
        clock.set(SECOND);
        tracker.record(1000L, 3, OVERWORLD);

        // vanilla reports the game time every 20 ticks, so once a second at 20 TPS; three of those
        // intervals pass with no report at all
        clock.set(SECOND + 3L * SECOND + 1L);

        assertEquals("TPS: -", tpsLine(false));
    }

    @Test
    void reportsNoRateWhileTheServerIsFrozen() {
        tracker.record(0L, 3, OVERWORLD);
        clock.set(SECOND);
        tracker.record(0L, 3, OVERWORLD);

        assertEquals("TPS: -", tpsLine(true));
    }

    private String tpsLine(boolean frozen) {
        return NetworkHud.tpsLine(tracker, VANILLA_TICK_RATE, frozen, 1, true).getString();
    }
}
