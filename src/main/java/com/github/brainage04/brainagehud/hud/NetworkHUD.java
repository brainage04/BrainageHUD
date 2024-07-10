package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;
import static com.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class NetworkHUD {
    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    public static void networkHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.NetworkHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        if (settings.showPing) {
            if (MinecraftClient.getInstance().getNetworkHandler() == null) { // singleplayer
                long ping =  0;

                for (int i = 0; i < MinecraftClient.getInstance().getDebugHud().getPingLog().getLength(); i++) {
                    ping += MinecraftClient.getInstance().getDebugHud().getPingLog().get(i);
                }

                ping /= MinecraftClient.getInstance().getDebugHud().getPingLog().getLength();

                lines.add(
                        String.format(
                                "Ping: %sms",
                                ping
                        )
                );
            } else { // multiplayer
                List<PlayerListEntry> playerList = MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream().toList();
                PlayerListEntry player = null;

                for (PlayerListEntry playerListEntry : playerList) {
                    if (playerListEntry.getProfile() == MinecraftClient.getInstance().getGameProfile()) {
                        player = playerListEntry;
                    }
                }

                if (player == null) {
                    return;
                }

                lines.add(
                        String.format(
                                "Ping: %sms",
                                player.getLatency()
                        )
                );
            }
        }

        if (settings.showTps) {
            if (MinecraftClient.getInstance().getServer() == null) {
                return;
            }

            float averageTps = 1000f / MinecraftClient.getInstance().getServer().getAverageTickTime();
            float maxTps = MinecraftClient.getInstance().getServer().getTickManager().getTickRate();

            lines.add(
                    String.format(
                            "TPS: %s",
                            roundDecimalPlaces(Math.min(averageTps, maxTps), settings.tpsDecimalPlaces)
                    )
            );
        }

        if (settings.showNetworkActivity) {
            // todo
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}