package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.FakeNanoClock;
import io.github.brainage04.brainagehud.util.TickRateTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.world.TickRateManager;
import org.junit.jupiter.api.Test;

class NetworkHudTest {
    private static final long SECOND = 1_000_000_000L;

    private final FakeNanoClock clock = new FakeNanoClock();
    private final TickRateTracker tracker = new TickRateTracker(clock);
    private final TickRateManager tickRateManager = new TickRateManager();

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
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);

        assertEquals("TPS: 20.0", tpsLine());
    }

    @Test
    void reportsNoRateWhenTheServerStopsSendingGameTime() {
        // Minemen sends its last game time report around login and then none at all, so the
        // tracker keeps the last interval it measured
        tracker.record(1000L, 3);
        clock.set(SECOND);
        tracker.record(1020L, 3);
        assertEquals("TPS: 20.0", tpsLine());

        // vanilla reports the game time every 20 ticks, so once a second at 20 TPS; three of those
        // intervals pass with no report at all
        clock.set(SECOND + 3L * SECOND + 1L);

        assertEquals("TPS: -", tpsLine());
    }

    @Test
    void reportsNoRateWhileTheServerIsFrozen() {
        // a healthy second first, so the tracker holds a real interval: a frozen server keeps
        // sending its game time without advancing it, and the pre-freeze window would otherwise
        // decay from 20 to 10 to 6.7 instead of showing nothing
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(20L, 3);
        assertEquals("TPS: 20.0", tpsLine());

        tickRateManager.setFrozen(true);

        assertEquals("TPS: -", tpsLine());
    }

    @Test
    void aSlowedServerIsMeasuredAgainstItsOwnTickRate() {
        tickRateManager.setTickRate(10.0F);
        tracker.record(0L, 3);
        clock.set(SECOND);
        tracker.record(10L, 3);

        assertEquals("TPS: 10.0", tpsLine());
    }

    private String tpsLine() {
        return NetworkHud.tpsLine(tracker, tickRateManager, 1, true).getString();
    }
}
