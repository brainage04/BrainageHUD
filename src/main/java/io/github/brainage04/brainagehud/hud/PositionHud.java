package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class PositionHud implements BasicHudElement<PositionHudConfig> {
    private static String getYawString(float yaw, String yawString) {
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
        return yawString;
    }

    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>(List.of());

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return lines;

        if (getElementConfig().showPosition) {
            lines.add(String.format("X: %s", roundDecimalPlaces(player.getX(), getElementConfig().positionDecimalPlaces)));
            lines.add(String.format("Y: %s", roundDecimalPlaces(player.getY(), getElementConfig().positionDecimalPlaces)));
            lines.add(String.format("Z: %s", roundDecimalPlaces(player.getZ(), getElementConfig().positionDecimalPlaces)));
        }

        if (getElementConfig().cCounter) {
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

        if (getElementConfig().eCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            lines.add(
                    MinecraftClient.getInstance().worldRenderer.getEntitiesDebugString().split(", SD: ")[0]
            );
        }

        if (getElementConfig().showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = MinecraftClient.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = MathHelper.wrapDegrees(entity.getYaw());
            String yawString = "";

            // this is cancer
            yawString = getYawString(yaw, yawString);

            float pitch = MathHelper.wrapDegrees(entity.getPitch());

            if (getElementConfig().showRotation) {
                yawString += String.format(
                        " (%s / %s)",
                        roundDecimalPlaces(yaw, getElementConfig().rotationDecimalPlaces),
                        roundDecimalPlaces(pitch, getElementConfig().rotationDecimalPlaces)
                );
            }

            lines.add(yawString);
        }

        return lines;
    }

    @Override
    public PositionHudConfig getElementConfig() {
        return getConfig().positionHudConfig;
    }
}