package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import com.github.brainage04.brainagehud.config.core.ElementCorners;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class HudRenderer {
    private static final List<HudElement<? extends CoreSettingsContainer>> REGISTERED_ELEMENTS = new ArrayList<>();

    public static void registerHudElement(HudElement<? extends CoreSettingsContainer> hudElement) {
        REGISTERED_ELEMENTS.add(hudElement);
    }

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
        ElementCorners corners = getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);
        HudElementEditor.CORE_SETTINGS_ELEMENTS.get(coreSettings.elementId).corners = corners;

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
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getScaledWidth() - elementWidth - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> (getScaledWidth() - elementWidth) / 2;
            default -> getConfig().elementPadding * 2;
        };
    }

    public static int getXOffsetNoPadding(ElementAnchor elementAnchor, int elementWidth) {
        return switch (elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getScaledWidth() - elementWidth;
            case TOP, CENTER, BOTTOM -> (getScaledWidth() - elementWidth) / 2;
            default -> 0;
        };
    }

    public static int getPosX(CoreSettings coreSettings, int elementWidth) {
        return coreSettings.x + getXOffset(coreSettings.elementAnchor, elementWidth);
    }

    public static int getYOffset(ElementAnchor elementAnchor, int elementHeight) {
        return switch (elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (getScaledHeight() - elementHeight) / 2 + getConfig().elementPadding;
            default -> getConfig().elementPadding * 2;
        };
    }

    public static int getYOffsetNoPadding(ElementAnchor elementAnchor, int elementHeight) {
        return switch (elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (getScaledHeight() - elementHeight) / 2;
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

        for (HudElement<? extends CoreSettingsContainer> hudElement : REGISTERED_ELEMENTS) {
            if (!hudElement.getElementConfig().coreSettings.enabled) continue;

            if (hudElement instanceof BasicHudElement<?> basicHudElement) {
                renderElement(renderer, drawContext, basicHudElement.getLines(), hudElement.getElementConfig().coreSettings);
            } else if (hudElement instanceof CustomHudElement<?> customHudElement) {
                customHudElement.render(renderer, drawContext);
            }
        }
    }

    public static ElementCorners getCornersWithPadding(int left, int top, int right, int bottom) {
        return new ElementCorners(
                left - getConfig().elementPadding * 2,
                top - getConfig().elementPadding * 2,
                right + getConfig().elementPadding * 2,
                bottom
        );
    }

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}