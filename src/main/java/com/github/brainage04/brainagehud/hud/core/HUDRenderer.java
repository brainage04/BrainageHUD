package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import com.github.brainage04.brainagehud.hud.*;
import com.github.brainage04.brainagehud.hud.custom.ArmourInfoHUD;
import com.github.brainage04.brainagehud.hud.custom.KeystrokesHUD;
import com.github.brainage04.brainagehud.util.ElementCorners;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class HUDRenderer {
    // for rendering HUD elements
    public static void renderElement(TextRenderer renderer, DrawContext drawContext, List<String> lines, CoreSettings coreSettings) {
        int elementWidth = 0;

        int lineHeight = renderer.fontHeight + getConfig().elementPadding;
        int elementHeight = lineHeight * lines.size() + getConfig().elementPadding;

        // vertical adjustments
        int posY = getPosY(coreSettings, elementHeight);

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = getPosX(coreSettings, lineWidth);

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
        int posX = getPosX(coreSettings, elementWidth);

        // adjust for padding
        ElementCorners corners = RenderUtils.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);
        RenderUtils.CORE_SETTINGS_ELEMENTS.get(coreSettings.elementId).corners = corners;

        // render backdrop
        int backdropOpacity;
        if (coreSettings.overrideGlobalBackdropOpacity) {
            backdropOpacity = coreSettings.backdropOpacity;
        } else {
            backdropOpacity = getConfig().backdropOpacity;
        }

        if (backdropOpacity > 0) {
            drawContext.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    -1,
                    backdropOpacity << 24
            );
        }
    }

    public static int getXOffset(ElementAnchor elementAnchor, int elementWidth) {
        return switch (elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> RenderUtils.getScaledWidth() - elementWidth - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> (RenderUtils.getScaledWidth() - elementWidth) / 2;
            default -> getConfig().elementPadding * 2;
        };
    }

    public static int getXOffsetNoPadding(ElementAnchor elementAnchor, int elementWidth) {
        return switch (elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> RenderUtils.getScaledWidth() - elementWidth;
            case TOP, CENTER, BOTTOM -> (RenderUtils.getScaledWidth() - elementWidth) / 2;
            default -> 0;
        };
    }

    private static int getPosX(CoreSettings coreSettings, int elementWidth) {
        return coreSettings.x + getXOffset(coreSettings.elementAnchor, elementWidth);
    }

    public static int getYOffset(ElementAnchor elementAnchor, int elementHeight) {
        return switch (elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> RenderUtils.getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (RenderUtils.getScaledHeight() - elementHeight) / 2 + getConfig().elementPadding;
            default -> getConfig().elementPadding * 2;
        };
    }

    public static int getYOffsetNoPadding(ElementAnchor elementAnchor, int elementHeight) {
        return switch (elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> RenderUtils.getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (RenderUtils.getScaledHeight() - elementHeight) / 2;
            default -> 0;
        };
    }

    public static int getPosY(CoreSettings coreSettings, int elementHeight) {
        int posY = coreSettings.y + getYOffset(coreSettings.elementAnchor, elementHeight);

        if (coreSettings.elementAnchor == ElementAnchor.TOP_RIGHT) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && !player.getStatusEffects().isEmpty() && getConfig().adjustTopRightElementsWithStatusEffects) {
                posY += getConfig().adjustTopRightElementsWithStatusEffectsAmount;
            }
        }

        return posY;
    }

    public static void render(DrawContext drawContext) {
        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        ArmourInfoHUD.render(renderer, drawContext, getConfig().armourInfoHudConfig);
        DateTimeHUD.render(renderer, drawContext, getConfig().dateTimeHudConfig);
        KeystrokesHUD.render(renderer, drawContext, getConfig().keystrokesHudConfig);
        NetworkHUD.render(renderer, drawContext, getConfig().networkHudConfig);
        PerformanceHUD.render(renderer, drawContext, getConfig().performanceHudConfig);
        PositionHUD.render(renderer, drawContext, getConfig().positionHudConfig);
        ReachHUD.render(renderer, drawContext, getConfig().reachHUDConfig);
        ToggleSprintHUD.render(renderer, drawContext, getConfig().toggleSprintHudConfig);
    }
}