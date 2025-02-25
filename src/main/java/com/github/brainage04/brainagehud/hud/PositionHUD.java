package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.ModConfig;
import com.github.brainage04.brainagehud.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;
import static com.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class PositionHUD {
    public static void positionHud(TextRenderer renderer, DrawContext drawContext, ModConfig.PositionHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of());

        if (settings.showPosition) {
            lines.add(String.format("X: %s", roundDecimalPlaces(MinecraftClient.getInstance().player.getX(), settings.positionDecimalPlaces)));
            lines.add(String.format("Y: %s", roundDecimalPlaces(MinecraftClient.getInstance().player.getY(), settings.positionDecimalPlaces)));
            lines.add(String.format("Z: %s", roundDecimalPlaces(MinecraftClient.getInstance().player.getZ(), settings.positionDecimalPlaces)));
        }

        if (settings.cCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            int completedChunks = MinecraftClient.getInstance().worldRenderer.getCompletedChunkCount();
            int totalChunks = (int) MinecraftClient.getInstance().worldRenderer.getChunkCount();

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "C: %d/%d %s",
                            completedChunks,
                            totalChunks,
                            MinecraftClient.getInstance().chunkCullingEnabled ? "(s)" : ""
                    )
            );
        }

        if (settings.eCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            lines.add(
                    MinecraftClient.getInstance().worldRenderer.getEntitiesDebugString().split(", SD: ")[0]
            );
        }

        if (settings.showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = MinecraftClient.getInstance().getCameraEntity();

            if (entity == null) {
                return;
            }

            float yaw = MathHelper.wrapDegrees(entity.getYaw());
            String yawString = "";

            // this is cancer
            if (MathUtils.isBetween(yaw, 157.5f, 180.0f) || MathUtils.isBetween(yaw, -180.0f, -157.5f)) {
                yawString = "N (-Z)";
            } else if (MathUtils.isBetween(yaw, -157.5f, -112.5f)) {
                yawString = "NE (+X, -Z)";
            } else if (MathUtils.isBetween(yaw, -112.5f, -67.5f)) {
                yawString = "E (+X)";
            } else if (MathUtils.isBetween(yaw, -67.5f, -22.5f)) {
                yawString = "SE (+X, +Z)";
            } else if (MathUtils.isBetween(yaw, -22.5f, 22.5f)) {
                yawString = "S (+Z)";
            } else if (MathUtils.isBetween(yaw, 22.5f, 67.5f)) {
                yawString = "SW (-X, +Z)";
            } else if (MathUtils.isBetween(yaw, 67.5f, 112.5f)) {
                yawString = "W (-X)";
            } else if (MathUtils.isBetween(yaw, 112.5f, 157.5f)) {
                yawString = "NW (-X, -Z)";
            }

            float pitch = MathHelper.wrapDegrees(entity.getPitch());

            if (settings.showRotation) {
                yawString += String.format(
                        " (%s / %s)",
                        roundDecimalPlaces(yaw, settings.rotationDecimalPlaces),
                        roundDecimalPlaces(pitch, settings.rotationDecimalPlaces)
                );
            }

            lines.add(yawString);
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}