package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.ClicksPerSecondFormat;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsElement;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.CustomHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class KeystrokesHud implements CustomHudElement<KeystrokesHudConfig> {
    public boolean isLeftClickShown() {
        return getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.LEFT_CLICK
                || getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }

    public boolean isRightClickShown() {
        return getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.RIGHT_CLICK
                || getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }

    private record KeyStrokesItem(@Nullable KeyBinding key, String name, int x, int y, int width, int height) {}

    public static boolean leftLastFrame = false;
    public static boolean rightLastFrame = false;

    public static final List<Long> leftCpsTimes = new ArrayList<>(List.of());
    public static final List<Long> rightCpsTimes = new ArrayList<>(List.of());

    private static List<KeyStrokesItem> getKeyStrokesItems(KeystrokesHudConfig settings) {
        int keySize = 20;

        // custom rendering logic
        int x1 = 0;
        int y1 = 0;

        List<KeyStrokesItem> keystrokesList = new ArrayList<>(List.of());

        int elementPadding = HudRendererLib.getPadding(settings.coreSettings);

        if (settings.showWasd) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.forwardKey,
                            "W",
                            x1 + keySize + elementPadding,
                            y1,
                            keySize,
                            keySize
                    )
            );

            y1 += keySize + elementPadding;

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
                            x1 + keySize + elementPadding,
                            y1,
                            keySize,
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.rightKey,
                            "D",
                            x1 + (keySize + elementPadding) * 2,
                            y1,
                            keySize,
                            keySize
                    )
            );

            y1 += keySize + elementPadding;
        }

        if (settings.showSpace) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.jumpKey,
                            "_____",
                            x1,
                            y1,
                            keySize * 3 + elementPadding * 2,
                            keySize
                    )
            );

            y1 += keySize + elementPadding;
        }

        if (settings.showMouseButtons) {
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.attackKey,
                            "LMB",
                            x1,
                            y1,
                            (int) (keySize * 1.5 + (double) elementPadding / 2),
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            MinecraftClient.getInstance().options.useKey,
                            "RMB",
                            x1 + (int) (keySize * 1.5 + (double) elementPadding / 2) + elementPadding,
                            y1,
                            (int) (keySize * 1.5 + (double) elementPadding / 2),
                            keySize
                    )
            );

            y1 += keySize + elementPadding;
        }

        if (settings.clicksPerSecondFormat != ClicksPerSecondFormat.NONE) {
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
                            keySize * 3 + elementPadding * 2,
                            keySize
                    )
            );
        }

        return keystrokesList;
    }

    @Override
    public void render(TextRenderer renderer, DrawContext drawContext) {
        List<KeyStrokesItem> keystrokesList = getKeyStrokesItems(getElementConfig());

        int elementWidth = keystrokesList.getLast().x + keystrokesList.getLast().width;
        int elementHeight = keystrokesList.getLast().y + keystrokesList.getLast().height;

        // horizontal adjustments (for element)
        int elementPadding = HudRendererLib.getPadding(getElementConfig().coreSettings);

        int posX = switch (getElementConfig().coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getElementConfig().coreSettings.x + (HudRenderer.getScaledWidth() - elementWidth) - elementPadding * 2;
            case TOP, CENTER, BOTTOM -> getElementConfig().coreSettings.x + (HudRenderer.getScaledWidth() - elementWidth) / 2;
            default -> getElementConfig().coreSettings.x + elementPadding * 2;
        };
        // vertical adjustments
        int posY = HudRenderer.getPosY(getElementConfig().coreSettings, elementHeight);
        // additional adjustments
        switch (getElementConfig().coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= elementPadding;
        }

        // determine bounds of HUD element
        ElementCorners corners = HudRenderer.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight, getElementConfig().coreSettings);
        corners.bottom += elementPadding * 2;

        CoreSettingsElement coreSettingsElement = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(getElementConfig().coreSettings.elementId);
        coreSettingsElement.corners = corners;
        HudElementEditor.CORE_SETTINGS_ELEMENTS.put(getElementConfig().coreSettings.elementId, coreSettingsElement);

        // render backdrop
        int backdropOpacity = HudRendererLib.getOpacity(getElementConfig().coreSettings);
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

        for (KeyStrokesItem keyStrokesItem : keystrokesList) {
            int backdropColour = getElementConfig().keyBackdropOpacity << 24;
            int textColour = HudRendererLib.getTextColour(getElementConfig().coreSettings);

            if (keyStrokesItem.key != null) {
                if (keyStrokesItem.key.isPressed()) {
                    backdropColour += HudRendererLib.getTextColour(getElementConfig().coreSettings);
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
            boolean textShadows = HudRendererLib.getTextShadows(getElementConfig().coreSettings);
            drawContext.drawText(
                    renderer,
                    keyStrokesItem.name,
                    posX + keyStrokesItem.x + (keyStrokesItem.width / 2) - (renderer.getWidth(keyStrokesItem.name) / 2),
                    posY + keyStrokesItem.y + (keyStrokesItem.height / 2) - (renderer.fontHeight / 2),
                    textColour,
                    textShadows
            );
        }
    }

    @Override
    public KeystrokesHudConfig getElementConfig() {
        return getConfig().keystrokesHudConfig;
    }
}
