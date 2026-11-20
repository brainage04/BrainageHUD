package io.github.brainage04.brainagehud.hud;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.util.TickRateTracker;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.TickRateManager;

public class NetworkHud implements BasicCoreHudElement<NetworkHudConfig> {
    private static final TickRateManager DEFAULT_TICK_RATE = new TickRateManager();

    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        boolean colour = getElementConfig().colourValues;

        if (getElementConfig().showPing) {
            long ping = ModPacketEvents.getPing();
            lines.add(line("Ping: ", ping + "ms", colour ? pingColour(ping) : null));
        }

        if (getElementConfig().showTps) {
            lines.add(
                    tpsLine(
                            ModPacketEvents.tickRateTracker(),
                            serverTickRate(),
                            isFrozen(),
                            getElementConfig().tpsDecimalPlaces,
                            colour));
        }

        return lines;
    }

    static Component tpsLine(
            TickRateTracker tracker,
            float tickRate,
            boolean frozen,
            int decimalPlaces,
            boolean colour) {
        // a frozen server keeps reporting its game time without advancing it, so its measured rate
        // is zero and says nothing about its health
        if (frozen || !tracker.hasRate(tickRate)) return Component.literal("TPS: -");

        // network jitter can make a healthy server appear to tick slightly faster than its target
        double tps = Math.min(tracker.ticksPerSecond(), tickRate);
        return line("TPS: ", roundDecimalPlaces(tps, decimalPlaces), colour ? tpsColour(tps) : null);
    }

    private static Component line(String label, String value, ChatFormatting colour) {
        MutableComponent valueText = Component.literal(value);
        if (colour != null) valueText.withStyle(colour);
        return Component.literal(label).append(valueText);
    }

    static ChatFormatting pingColour(long ping) {
        if (ping < 50L) return ChatFormatting.DARK_GREEN;
        if (ping < 100L) return ChatFormatting.GREEN;
        if (ping < 200L) return ChatFormatting.YELLOW;
        if (ping < 300L) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    static ChatFormatting tpsColour(double tps) {
        if (tps > 19.0D) return ChatFormatting.DARK_GREEN;
        if (tps > 18.0D) return ChatFormatting.GREEN;
        if (tps > 15.0D) return ChatFormatting.GOLD;
        if (tps > 10.0D) return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
    }

    /** The server's target tick rate, which it sends the client in its ticking state packet. */
    private static float serverTickRate() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? DEFAULT_TICK_RATE.tickrate() : level.tickRateManager().tickrate();
    }

    private static boolean isFrozen() {
        ClientLevel level = Minecraft.getInstance().level;
        return level != null && level.tickRateManager().isFrozen();
    }

    @Override
    public NetworkHudConfig getElementConfig() {
        return getConfig().networkHudConfig;
    }
}
