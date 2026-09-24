package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class NetworkHud implements BasicCoreHudElement<NetworkHudConfig> {
    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        boolean colour = getElementConfig().colourValues;

        if (getElementConfig().showPing) {
            long ping = ModPacketEvents.getPing();
            lines.add(line("Ping: ", ping + "ms", colour ? pingColour(ping) : null));
        }

        if (getElementConfig().showTps) {
            if (!ModPacketEvents.hasTps()) {
                lines.add("TPS: -");
            } else {
                double tps = currentTps();
                lines.add(line("TPS: ", roundDecimalPlaces(tps, getElementConfig().tpsDecimalPlaces), colour ? tpsColour(tps) : null));
            }
        }

        return lines;
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

    private static double currentTps() {
        // network jitter can make a healthy server appear to tick slightly faster than its target
        double tps = ModPacketEvents.getTps();
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) tps = Math.min(tps, level.tickRateManager().tickrate());
        return tps;
    }

    @Override
    public NetworkHudConfig getElementConfig() {
        return getConfig().networkHudConfig;
    }
}
