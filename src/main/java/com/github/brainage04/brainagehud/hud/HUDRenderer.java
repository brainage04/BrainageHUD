package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.util.MathUtils;
import com.github.brainage04.brainagehud.util.RenderUtils;
import com.github.brainage04.brainagehud.util.TimerUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static com.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class HUDRenderer implements HudRenderCallback {
    // for rendering HUD elements
    public static void renderElement(TextRenderer renderer, DrawContext drawContext, List<String> lines, BrainageHUDConfig.CoreSettings coreSettings) {
        int elementWidth = 0;

        int lineHeight = renderer.fontHeight + getConfig().padding;
        int elementHeight = lineHeight * lines.size() + getConfig().padding;

        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int scaledHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        // vertical adjustments
        int posY = switch (coreSettings.elementAnchor) {
            case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> scaledHeight - coreSettings.y - renderer.fontHeight;
            case LEFT, CENTER, RIGHT -> (scaledHeight - coreSettings.y - renderer.fontHeight) / 2;
            default -> coreSettings.y;
        };

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = switch (coreSettings.elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> scaledWidth - coreSettings.x - lineWidth;
                case TOP, CENTER, BOTTOM -> (scaledWidth - coreSettings.x - lineWidth) / 2;
                default -> coreSettings.x;
            };

            drawContext.drawText(
                    renderer,
                    lines.get(i),
                    posX,
                    posY + (lineHeight * i),
                    getConfig().primaryTextColour,
                    getConfig().textShadows
            );
        }

        // horizontal adjustments (for element)
        int posX = switch (coreSettings.elementAnchor) {
            case TOPRIGHT, RIGHT, BOTTOMRIGHT -> scaledWidth - coreSettings.x - elementWidth;
            case TOP, CENTER, BOTTOM -> (scaledWidth - coreSettings.x - elementWidth) / 2;
            default -> coreSettings.x;
        };

        // adjust for padding
        int[] corners = RenderUtils.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);

        // store corners for use in BrainageHUDElementEditor
        RenderUtils.elementCorners.get(coreSettings.elementId)[0] = corners[0];
        RenderUtils.elementCorners.get(coreSettings.elementId)[1] = corners[1];
        RenderUtils.elementCorners.get(coreSettings.elementId)[2] = corners[2];
        RenderUtils.elementCorners.get(coreSettings.elementId)[3] = corners[3];

        if (getConfig().backdrop) {
            // draw rectangle
            drawContext.fill(
                    corners[0],
                    corners[1],
                    corners[2],
                    corners[3],
                    -1,
                    getConfig().backdropColour
            );
        }
    }

    // for bare bones rendering
    public static void renderElement(TextRenderer renderer, DrawContext drawContext, List<String> lines, int x, int y, BrainageHUDConfig.ElementAnchor elementAnchor) {
        int elementWidth = 0;

        int lineHeight = renderer.fontHeight + getConfig().padding;
        int elementHeight = lineHeight * lines.size() + getConfig().padding;

        int scaledWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int scaledHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        // vertical adjustments
        int posY = switch (elementAnchor) {
            case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> scaledHeight - y - renderer.fontHeight;
            case LEFT, CENTER, RIGHT -> (scaledHeight - y - renderer.fontHeight) / 2;
            default -> y;
        };

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = switch (elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> scaledWidth - x - lineWidth;
                case TOP, CENTER, BOTTOM -> (scaledWidth - x - lineWidth) / 2;
                default -> x;
            };

            drawContext.drawText(
                    renderer,
                    lines.get(i),
                    posX,
                    posY + (lineHeight * i),
                    getConfig().primaryTextColour,
                    getConfig().textShadows
            );
        }
    }


    public void positionHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.PositionHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        List<String> lines = new ArrayList<>(Arrays.asList());

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

    public void dateTimeHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.DateTimeHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(Arrays.asList());

        if (settings.showDate) {
            lines.add(new SimpleDateFormat("E d/M/y").format(new Date()));
        }

        if (settings.showTime) {
            if (settings.twelveHourFormat) {
                lines.add(new SimpleDateFormat("h:mm:ss a").format(new Date()));
            } else {
                lines.add(new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
        }

        if (settings.showTimezone) {
            lines.add(
                    String.format(
                            "%s (UTC %s)",
                            new SimpleDateFormat("z").format(new Date()),
                            new SimpleDateFormat("XXX").format(new Date())
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }

    public void toggleSprintHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.ToggleSprintHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        List<String> lines = new ArrayList<>(Arrays.asList(Formatting.GRAY + "[Walking (Vanilla)]"));

        if (MinecraftClient.getInstance().player.isSprinting()) {
            if (MinecraftClient.getInstance().options.getSprintToggled().getValue() && MinecraftClient.getInstance().options.sprintKey.isPressed()) {
                lines.set(0, "[Sprinting (Toggled)]");
            } else {
                lines.set(0, "[Sprinting (Vanilla)]");
            }
        }

        if (MinecraftClient.getInstance().player.isSneaking()) {
            if (MinecraftClient.getInstance().options.getSneakToggled().getValue() && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
                lines.set(0, "[Sneaking (Toggled)]");
            } else {
                lines.set(0, "[Sneaking (Vanilla)]");
            }
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }

    public void performanceHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.PerformanceHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(Arrays.asList());

        if (settings.showFps) {
            lines.add(
                    String.format(
                            "%d FPS",
                            MinecraftClient.getInstance().getCurrentFps()
                    )
            );
        }

        // taken from net.minecraft.client.gui.hud.DebugHud
        if (settings.showRamUsage) {
            long l = Runtime.getRuntime().maxMemory();
            long m = Runtime.getRuntime().totalMemory();
            long n = Runtime.getRuntime().freeMemory();
            long o = m - n;

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "RAM: %d%% (%d/%dMB)",
                            o * 100L / l,
                            o / 1_048_576L,
                            l / 1_048_576L
                    )
            );
        }

        if (settings.showGpuUsage) {
            TimerUtils.updateGpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "GPU: %d%%",
                            TimerUtils.GPU_USAGE
                    )
            );
        }

        if (settings.showCpuUsage) {
            TimerUtils.updateCpuUsage(500);

            lines.add(
                    String.format(
                            Locale.ROOT,
                            "CPU: %d%%",
                            TimerUtils.CPU_USAGE
                    )
            );
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }

    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    public void networkHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.NetworkHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<String> lines = new ArrayList<>(Arrays.asList());

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

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        positionHud(renderer, drawContext, getConfig().positionHudConfig);
        dateTimeHud(renderer, drawContext, getConfig().dateTimeHudConfig);
        toggleSprintHud(renderer, drawContext, getConfig().toggleSprintHudConfig);
        performanceHud(renderer, drawContext, getConfig().performanceHudConfig);
        networkHud(renderer, drawContext, getConfig().networkHudConfig);
    }
}
