package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.ClicksPerSecondFormat;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.ARGB;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class KeystrokesHud implements CoreHudElement<KeystrokesHudConfig> {
    public boolean isLeftClickShown() {
        return getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.LEFT_CLICK
                || getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }

    public boolean isRightClickShown() {
        return getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.RIGHT_CLICK
                || getElementConfig().clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }

    private static final long KEY_TRANSITION_DURATION_NANOS = 100_000_000L;

    private record KeyStrokesItem(@Nullable KeyMapping key, String name, int x, int y, int width, int height) {}

    private static final class KeyTransition {
        private float progress;
        private boolean down;
        private long lastUpdateNanos;
        private long lastSeenFrame;

        private KeyTransition(boolean down, long nowNanos, long frame) {
            this.progress = down ? 1.0F : 0.0F;
            this.down = down;
            this.lastUpdateNanos = nowNanos;
            this.lastSeenFrame = frame;
        }
    }

    public static boolean leftLastFrame = false;
    public static boolean rightLastFrame = false;

    public static final List<Long> leftCpsTimes = new ArrayList<>(List.of());
    public static final List<Long> rightCpsTimes = new ArrayList<>(List.of());

    private final IdentityHashMap<KeyMapping, KeyTransition> keyTransitions = new IdentityHashMap<>();
    private long renderFrame;

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
                            Minecraft.getInstance().options.keyUp,
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
                            Minecraft.getInstance().options.keyLeft,
                            "A",
                            x1,
                            y1,
                            keySize,
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            Minecraft.getInstance().options.keyDown,
                            "S",
                            x1 + keySize + elementPadding,
                            y1,
                            keySize,
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            Minecraft.getInstance().options.keyRight,
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
                            Minecraft.getInstance().options.keyJump,
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
                            Minecraft.getInstance().options.keyAttack,
                            "LMB",
                            x1,
                            y1,
                            (int) (keySize * 1.5 + (double) elementPadding / 2),
                            keySize
                    )
            );
            keystrokesList.add(
                    new KeyStrokesItem(
                            Minecraft.getInstance().options.keyUse,
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

    private static void updateCpsTimes(List<Long> clickTimes, long nowMillis) {
        while (!clickTimes.isEmpty() && nowMillis - clickTimes.getFirst() > 1000L) {
            clickTimes.removeFirst();
        }
    }

    private void updateClicksPerSecond(KeystrokesHudConfig settings, Minecraft minecraft, long nowMillis) {
        if (settings.showMouseButtons && isLeftClickShown()) {
            if (minecraft.options.keyAttack.isDown()) {
                if (!leftLastFrame) {
                    leftCpsTimes.add(nowMillis);
                }
                leftLastFrame = true;
            } else {
                leftLastFrame = false;
            }
            updateCpsTimes(leftCpsTimes, nowMillis);
        }

        if (settings.showMouseButtons && isRightClickShown()) {
            if (minecraft.options.keyUse.isDown()) {
                if (!rightLastFrame) {
                    rightCpsTimes.add(nowMillis);
                }
                rightLastFrame = true;
            } else {
                rightLastFrame = false;
            }
            updateCpsTimes(rightCpsTimes, nowMillis);
        }
    }

    private static float advanceTransition(KeyTransition transition, boolean down, long nowNanos) {
        long elapsedNanos = Math.max(0L, nowNanos - transition.lastUpdateNanos);
        float amount = Math.min(1.0F, (float) elapsedNanos / KEY_TRANSITION_DURATION_NANOS);
        if (transition.down) {
            transition.progress = Math.min(1.0F, transition.progress + amount);
        } else {
            transition.progress = Math.max(0.0F, transition.progress - amount);
        }
        transition.down = down;
        transition.lastUpdateNanos = nowNanos;
        return transition.progress;
    }

    @Override
    public void render(GuiGraphicsExtractor drawContext, DeltaTracker tickCounter) {
        KeystrokesHudConfig settings = getElementConfig();
        Minecraft minecraft = Minecraft.getInstance();
        List<KeyStrokesItem> keystrokesList = getKeyStrokesItems(settings);
        if (keystrokesList.isEmpty()) return;
        int elementWidth = keystrokesList.getLast().x + keystrokesList.getLast().width;
        int elementHeight = keystrokesList.getLast().y + keystrokesList.getLast().height;
        int elementPadding = HudRendererLib.getPadding(settings.coreSettings);
        int posX = HudRenderer.getPosX(settings.coreSettings, elementWidth);
        int posY = HudRenderer.getPosY(settings.coreSettings, elementHeight);
        switch (settings.coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= elementPadding;
        }

        ElementCorners corners = HudRenderer.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight, settings.coreSettings);
        corners.bottom += elementPadding * 2;
        HudRenderer.setElementBounds(settings.coreSettings, corners);
        HudRenderer.renderBackdrop(drawContext, corners, settings.coreSettings);

        updateClicksPerSecond(settings, minecraft, System.currentTimeMillis());

        long frame = ++renderFrame;
        long nowNanos = System.nanoTime();
        int configuredTextColour = HudRendererLib.getTextColour(settings.coreSettings);
        int keyBackdropAlpha = settings.keyBackdropOpacity;
        Font renderer = minecraft.font;
        boolean textShadows = HudRendererLib.getTextShadows(settings.coreSettings);
        for (KeyStrokesItem keyStrokesItem : keystrokesList) {
            int backdropColour = ARGB.color(keyBackdropAlpha, 0, 0, 0);
            int textColour = configuredTextColour;
            if (keyStrokesItem.key != null) {
                boolean down = keyStrokesItem.key.isDown();
                KeyTransition transition = keyTransitions.get(keyStrokesItem.key);
                if (transition == null) {
                    transition = new KeyTransition(down, nowNanos, frame);
                    keyTransitions.put(keyStrokesItem.key, transition);
                } else {
                    advanceTransition(transition, down, nowNanos);
                    transition.lastSeenFrame = frame;
                }

                int pressedBackdropColour = ARGB.color(
                        keyBackdropAlpha,
                        ARGB.red(configuredTextColour),
                        ARGB.green(configuredTextColour),
                        ARGB.blue(configuredTextColour)
                );
                backdropColour = ARGB.srgbLerp(transition.progress, backdropColour, pressedBackdropColour);
                textColour = ARGB.srgbLerp(transition.progress, configuredTextColour, ARGB.opaque(0));
            }

            drawContext.fill(
                    posX + keyStrokesItem.x,
                    posY + keyStrokesItem.y,
                    posX + keyStrokesItem.x + keyStrokesItem.width,
                    posY + keyStrokesItem.y + keyStrokesItem.height,
                    backdropColour
            );
            drawContext.text(
                    renderer,
                    keyStrokesItem.name,
                    posX + keyStrokesItem.x + (keyStrokesItem.width / 2) - (renderer.width(keyStrokesItem.name) / 2),
                    posY + keyStrokesItem.y + (keyStrokesItem.height / 2) - (renderer.lineHeight / 2),
                    textColour,
                    textShadows
            );
        }
        keyTransitions.entrySet().removeIf(entry -> entry.getValue().lastSeenFrame != frame);
    }

    @Override
    public KeystrokesHudConfig getElementConfig() {
        return getConfig().keystrokesHudConfig;
    }
}
