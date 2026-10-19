package io.github.brainage04.brainagehud.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.ChatFormatting;
import org.junit.jupiter.api.Test;

class NetworkHudTest {
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
}
