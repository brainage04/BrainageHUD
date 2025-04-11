package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

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
    public TextList getLines() {
        TextList lines = new TextList();

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return lines;

        if (getElementConfig().showPosition) {
            lines.add("X: %s".formatted(roundDecimalPlaces(player.getX(), getElementConfig().positionDecimalPlaces)));
            lines.add("Y: %s".formatted(roundDecimalPlaces(player.getY(), getElementConfig().positionDecimalPlaces)));
            lines.add("Z: %s".formatted(roundDecimalPlaces(player.getZ(), getElementConfig().positionDecimalPlaces)));
        }

        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;

        if (getElementConfig().cCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            int completedChunks = worldRenderer.getCompletedChunkCount();
            int totalChunks = (int) worldRenderer.getChunkCount();

            lines.add("C: %d/%d%s".formatted(
                    completedChunks,
                    totalChunks,
                    MinecraftClient.getInstance().chunkCullingEnabled ? " (s)" : ""
            ));
        }

        if (getElementConfig().eCounter) {
            // taken from net.minecraft.client.renderer.WorldRenderer
            lines.add("E: %d/%d".formatted(
                    worldRenderer.renderedEntitiesCount,
                    player.clientWorld.getRegularEntityCount()
            ));
        }

        if (getElementConfig().showDirection) {
            // taken from net.minecraft.client.gui.hud.DebugHud
            Entity entity = MinecraftClient.getInstance().getCameraEntity();
            if (entity == null) return lines;

            float yaw = MathHelper.wrapDegrees(entity.getYaw());
            String yawString = "";

            yawString = getYawString(yaw, yawString);

            float pitch = MathHelper.wrapDegrees(entity.getPitch());

            if (getElementConfig().showRotation) {
                yawString += " (%s / %s)".formatted(
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