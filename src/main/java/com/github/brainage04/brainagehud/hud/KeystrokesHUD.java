package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.ModConfig;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.brainage04.brainagehud.util.ConfigUtils.*;

public class KeystrokesHUD {
    // todo: why is the key nullable? surely there is a better approach
    private record KeyStrokesItem(@Nullable KeyBinding key, String name, int x, int y, int width, int height) {}

    public static boolean leftLastFrame = false;
    public static boolean rightLastFrame = false;

    public static final List<Long> leftCpsTimes = new ArrayList<>(List.of());
    public static final List<Long> rightCpsTimes = new ArrayList<>(List.of());

    public static void keystrokesHud(TextRenderer renderer, DrawContext drawContext, ModConfig.KeystrokesHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        int keySize = 20;

        // custom rendering logic
        int x1 = 0;
        int y1 = 0;

        List<KeyStrokesItem> keystrokesList = new ArrayList<>(List.of());

        if (settings.showWasd) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.forwardKey,
                            "W",
                            x1 + keySize + getConfig().elementPadding,
                            y1,
                            keySize,
                            keySize
                    )
            );

            y1 += keySize + getConfig().elementPadding;

            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.leftKey,
                            "A",
                            x1,
                            y1,
                            keySize,
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.backKey,
                            "S",
                            x1 + keySize + getConfig().elementPadding,
                            y1,
                            keySize,
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.rightKey,
                            "D",
                            x1 + (keySize + getConfig().elementPadding) * 2,
                            y1,
                            keySize,
                            keySize
                    )
            );

            y1 += keySize + getConfig().elementPadding;
        }

        if (settings.showSpace) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.jumpKey,
                            "_____",
                            x1,
                            y1,
                            keySize * 3 + getConfig().elementPadding * 2,
                            keySize
                    )
            );

            y1 += keySize + getConfig().elementPadding;
        }

        if (settings.showMouseButtons) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.attackKey,
                            "LMB",
                            x1,
                            y1,
                            (int) (keySize * 1.5 + getConfig().elementPadding / 2),
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.useKey,
                            "RMB",
                            x1 + (int) (keySize * 1.5 + getConfig().elementPadding / 2) + getConfig().elementPadding,
                            y1,
                            (int) (keySize * 1.5 + getConfig().elementPadding / 2),
                            keySize
                    )
            );

            y1 += keySize + getConfig().elementPadding;
        }

        if (settings.clicksPerSecondFormat != ModConfig.ClicksPerSecondFormat.NONE) {
            String cpsString = switch (settings.clicksPerSecondFormat) {
                case LEFT_CLICK -> "%d CPS".formatted(leftCpsTimes.size());
                case RIGHT_CLICK -> "%d CPS (R)".formatted(rightCpsTimes.size());
                case BOTH -> "%d | %d CPS".formatted(leftCpsTimes.size(), rightCpsTimes.size());
                default -> "";
            };

            keystrokesList.add(
                    new KeyStrokesItem(
                            null,
                            cpsString,
                            x1,
                            y1,
                            keySize * 3 + getConfig().elementPadding * 2,
                            keySize
                    )
            );
        }

        int elementWidth = keystrokesList.getLast().x + keystrokesList.getLast().width;
        int elementHeight = keystrokesList.getLast().y + keystrokesList.getLast().height;

        // horizontal adjustments (for element)
        int posX = switch (settings.coreSettings.elementAnchor) {
            case TOPRIGHT, RIGHT, BOTTOMRIGHT -> settings.coreSettings.x + (RenderUtils.getScaledWidth() - elementWidth) - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> settings.coreSettings.x + (RenderUtils.getScaledWidth() - elementWidth) / 2;
            default -> settings.coreSettings.x + getConfig().elementPadding * 2;
        };
        // vertical adjustments
        int posY = switch (settings.coreSettings.elementAnchor) {
            case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> settings.coreSettings.y + (RenderUtils.getScaledHeight() - elementHeight) - getConfig().elementPadding * 2;
            case LEFT, CENTER, RIGHT -> settings.coreSettings.y + (RenderUtils.getScaledHeight() - elementHeight) / 2;
            default -> settings.coreSettings.y + getConfig().elementPadding * 2;
        };

        // determine bounds of HUD element
        // adjust for padding
        int[] corners = RenderUtils.getCornersWithPadding(
                posX,
                posY,
                posX + elementWidth,
                posY + elementHeight
        );
        corners[3] += getConfig().elementPadding * 2;

        // store corners for use in HUDElementEditor
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[0] = corners[0];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[1] = corners[1];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[2] = corners[2];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[3] = corners[3];

        // render backdrop
        int backdropOpacity = settings.coreSettings.overrideGlobalBackdropOpacity ? settings.coreSettings.backdropOpacity : getConfig().backdropOpacity;

        if (backdropOpacity > 0) {
            drawContext.fill(
                    corners[0],
                    corners[1],
                    corners[2],
                    corners[3],
                    -1,
                    backdropOpacity << 24
            );
        }

        for (KeyStrokesItem keyStrokesItem : keystrokesList) {
            int backdropColour = settings.keyBackdropOpacity << 24;
            int textColour = getConfig().primaryTextColour;

            if (keyStrokesItem.key != null) {
                if (keyStrokesItem.key.isPressed()) {
                    backdropColour += getConfig().primaryTextColour;
                    textColour = 0x000000;

                    // measure left/right cps
                    if (isLeftClickShown()) {
                        if (Objects.equals(keyStrokesItem.key.getTranslationKey(), MinecraftClient.getInstance().options.attackKey.getTranslationKey())) {
                            if (!leftLastFrame) {
                                leftCpsTimes.add(System.currentTimeMillis());
                            }

                            leftLastFrame = true;
                        }
                    }

                    if (isRightClickShown()) {
                        if (Objects.equals(keyStrokesItem.key.getTranslationKey(), MinecraftClient.getInstance().options.useKey.getTranslationKey())) {
                            if (!rightLastFrame) {
                                rightCpsTimes.add(System.currentTimeMillis());
                            }

                            rightLastFrame = true;
                        }
                    }
                }
            }

            // update left/right cps
            if (isLeftClickShown()) {
                if (!MinecraftClient.getInstance().options.attackKey.isPressed()) {
                    leftLastFrame = false;
                }

                for (int i = 0; i < leftCpsTimes.size(); i++) {
                    long time = leftCpsTimes.get(i);
                    if (System.currentTimeMillis() - time > 1000) {
                        leftCpsTimes.remove(i);
                    } else {
                        // cps times are in chronological order
                        // once the first time under 1000ms ago is found,
                        // all times afterward will also be under 1000ms
                        break;
                    }
                }
            }

            if (isRightClickShown()) {
                if (!MinecraftClient.getInstance().options.useKey.isPressed()) {
                    rightLastFrame = false;
                }

                for (int i = 0; i < rightCpsTimes.size(); i++) {
                    long time = rightCpsTimes.get(i);
                    if (System.currentTimeMillis() - time > 1000) {
                        rightCpsTimes.remove(i);
                    } else {
                        // cps times are in chronological order
                        // once the first time under 1000ms ago is found,
                        // all times afterward will also be under 1000ms
                        break;
                    }
                }
            }

            // draw backdrop
            drawContext.fill(
                    posX + keyStrokesItem.x,
                    posY + keyStrokesItem.y,
                    posX + keyStrokesItem.x + keyStrokesItem.width,
                    posY + keyStrokesItem.y + keyStrokesItem.height,
                    backdropColour
            );

            // draw text in center of backdrop
            drawContext.drawText(
                    renderer,
                    keyStrokesItem.name,
                    posX + keyStrokesItem.x + (keyStrokesItem.width / 2) - (renderer.getWidth(keyStrokesItem.name) / 2),
                    posY + keyStrokesItem.y + (keyStrokesItem.height / 2) - (renderer.fontHeight / 2),
                    textColour,
                    getConfig().textShadows
            );
        }
    }
}
