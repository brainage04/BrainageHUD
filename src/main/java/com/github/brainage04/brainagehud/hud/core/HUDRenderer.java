package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.*;

import static com.github.brainage04.brainagehud.hud.NetworkHUD.networkHud;
import static com.github.brainage04.brainagehud.hud.PerformanceHUD.performanceHud;
import static com.github.brainage04.brainagehud.hud.PositionHUD.positionHud;
import static com.github.brainage04.brainagehud.hud.DateTimeHUD.dateTimeHud;
import static com.github.brainage04.brainagehud.hud.ToggleSprintHUD.toggleSprintHud;
import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class HUDRenderer implements HudRenderCallback {
    // for rendering HUD elements
    public static void renderElement(TextRenderer renderer, DrawContext drawContext, List<String> lines, BrainageHUDConfig.CoreSettings coreSettings) {
        int elementWidth = 0;

        int lineHeight = renderer.fontHeight + getConfig().elementPadding;
        int elementHeight = lineHeight * lines.size() + getConfig().elementPadding;

        // vertical adjustments
        int posY = switch (coreSettings.elementAnchor) {
            case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> coreSettings.y + (RenderUtils.scaledHeight - elementHeight);
            case LEFT, CENTER, RIGHT -> coreSettings.y + (RenderUtils.scaledHeight - elementHeight) / 2 + getConfig().elementPadding;
            default -> coreSettings.y + getConfig().elementPadding * 2;
        };

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = switch (coreSettings.elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> coreSettings.x + (RenderUtils.scaledWidth - lineWidth) - getConfig().elementPadding * 2;
                case TOP, CENTER, BOTTOM -> coreSettings.x + (RenderUtils.scaledWidth - lineWidth) / 2;
                default -> coreSettings.x + getConfig().elementPadding * 2;
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
            case TOPRIGHT, RIGHT, BOTTOMRIGHT -> coreSettings.x + (RenderUtils.scaledWidth - elementWidth) - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> coreSettings.x + (RenderUtils.scaledWidth - elementWidth) / 2;
            default -> coreSettings.x + getConfig().elementPadding * 2;
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

        int lineHeight = renderer.fontHeight + getConfig().elementPadding;
        int elementHeight = lineHeight * lines.size() + getConfig().elementPadding;

        // vertical adjustments
        int posY = switch (elementAnchor) {
            case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> y + (RenderUtils.scaledHeight - elementHeight);
            case LEFT, CENTER, RIGHT -> y + (RenderUtils.scaledHeight - elementHeight) / 2 + getConfig().elementPadding;
            default -> y + getConfig().elementPadding * 2;
        };

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = switch (elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT ->
                        x + (RenderUtils.scaledWidth - lineWidth) - getConfig().elementPadding * 2;
                case TOP, CENTER, BOTTOM -> x + (RenderUtils.scaledWidth - lineWidth) / 2;
                default -> x + getConfig().elementPadding * 2;
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