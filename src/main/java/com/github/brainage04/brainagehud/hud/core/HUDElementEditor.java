package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import com.github.brainage04.brainagehud.util.ElementCorners;
import com.github.brainage04.brainagehud.util.CoreSettingsElement;
import com.github.brainage04.brainagehud.util.MathUtils;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.*;

public class HUDElementEditor extends Screen {
    public int selectedElementIndex = -1;

    public double selectedElementX = -1;
    public double selectedElementY = -1;

    public double selectedMouseX = -1;
    public double selectedMouseY = -1;

    public int highlightedElementIndex = -1;

    private Integer previousMenuBackgroundBlurriness;

    public HUDElementEditor() {
        super(Text.of(BrainageHUD.MOD_NAME + " Element Editor"));

        RenderUtils.populateCoreSettingsElements();

        // disable blur while editing elements
        disableBlur();
    }

    public void disableBlur() {
        SimpleOption<Integer> menuBackgroundBlurriness = MinecraftClient.getInstance().options.getMenuBackgroundBlurriness();
        previousMenuBackgroundBlurriness = menuBackgroundBlurriness.getValue();
        menuBackgroundBlurriness.setValue(0);
    }

    public void revertBlur() {
        MinecraftClient.getInstance().options.getMenuBackgroundBlurriness().setValue(previousMenuBackgroundBlurriness);
    }

    public int mouseInElement(double mouseX, double mouseY) {
        for (int i = 0; i < RenderUtils.CORE_SETTINGS_ELEMENTS.size(); i++) {
            ElementCorners corners = RenderUtils.CORE_SETTINGS_ELEMENTS.get(i).corners;

            if (RenderUtils.mouseInRect(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    mouseX,
                    mouseY
            )) {
                return i;
            }
        }

        return -1;
    }

    public final ButtonWidget button1 = ButtonWidget.builder(Text.literal("Undo & Close"), button -> closeWithoutSaving())
            .dimensions(RenderUtils.getScaledWidth() / 2 - 210, RenderUtils.getScaledHeight() - 40, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Reverts the current positions to what they were before and closes the screen.")))
            .build();
    public final ButtonWidget button2 = ButtonWidget.builder(Text.literal("Save & Close"), button -> this.close())
            .dimensions(RenderUtils.getScaledWidth() / 2 + 10, RenderUtils.getScaledHeight() - 40, 200, 20)
            .tooltip(Tooltip.of(Text.literal("Saves the current positions and closes the screen.")))
            .build();

    public void updateElementPosition(int deltaX, int deltaY) {
        CoreSettingsElement coreSettingsElement = RenderUtils.CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
        ElementCorners corners = coreSettingsElement.corners;
        CoreSettings coreSettings = coreSettingsElement.coreSettings;
        int elementWidth = corners.right - corners.left;
        int elementHeight = corners.bottom - corners.top;

        int minX = switch (coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getConfig().screenMargin - (RenderUtils.getScaledWidth() - elementWidth);
            case TOP, CENTER, BOTTOM -> getConfig().screenMargin - (RenderUtils.getScaledWidth() - elementWidth) / 2;
            default -> getConfig().screenMargin;
        };
        int minY = switch (coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> getConfig().screenMargin - (RenderUtils.getScaledHeight() - elementHeight);
            case LEFT, CENTER, RIGHT -> getConfig().screenMargin - (RenderUtils.getScaledHeight() - elementHeight) / 2;
            default -> getConfig().screenMargin;
        };

        coreSettings.x = (int) (MathHelper.clamp(
                selectedElementX - deltaX,
                minX,
                minX + RenderUtils.getScaledWidth() - elementWidth - getConfig().screenMargin * 2
        ));
        coreSettings.y = (int) (MathHelper.clamp(
                selectedElementY - deltaY,
                minY,
                minY + RenderUtils.getScaledHeight() - elementHeight - getConfig().screenMargin * 2
        ));
    }

    @Override
    protected void init() {
        addDrawableChild(button1);
        addDrawableChild(button2);
    }

    @Override
    public void close() {
        saveConfig();
        revertBlur();
        super.close();
    }

    public void closeWithoutSaving() {
        loadConfig();
        revertBlur();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // from package net.minecraft.client.gui.screen.Screen;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.shouldCloseOnEsc()) {
            closeWithoutSaving();
            return true;
        }

        if (selectedElementIndex != -1) {
            CoreSettingsElement coreSettingsElement = RenderUtils.CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
            CoreSettings coreSettings = coreSettingsElement.coreSettings;

            if (keyCode == GLFW.GLFW_KEY_SPACE) {
                ElementCorners corners = coreSettingsElement.corners;

                ElementAnchor before = coreSettings.elementAnchor;
                int newOrdinal = coreSettings.elementAnchor.ordinal() + 1;
                if (newOrdinal >= ElementAnchor.values().length) newOrdinal = 0;
                coreSettings.elementAnchor = ElementAnchor.values()[newOrdinal];
                ElementAnchor after = coreSettings.elementAnchor;

                // modify x/y coords such that the element remains in the same position
                int beforeX = HUDRenderer.getXOffsetNoPadding(before, corners.right - corners.left);
                int afterX = HUDRenderer.getXOffsetNoPadding(after, corners.right - corners.left);
                int beforeY = HUDRenderer.getYOffsetNoPadding(before, corners.bottom - corners.top);
                int afterY = HUDRenderer.getYOffsetNoPadding(after, corners.bottom - corners.top);

                coreSettings.x += (beforeX - afterX);
                coreSettings.y += (beforeY - afterY);

                // update selected element position
                selectedElementX = coreSettings.x;
                selectedElementY = coreSettings.y;
            } else {
                int xDirection = 0;
                int yDirection = 0;

                switch (keyCode) {
                    case GLFW.GLFW_KEY_UP:
                        yDirection = 1;
                        break;
                    case GLFW.GLFW_KEY_DOWN:
                        yDirection = -1;
                        break;
                    case GLFW.GLFW_KEY_LEFT:
                        xDirection = 1;
                        break;
                    case GLFW.GLFW_KEY_RIGHT:
                        xDirection = -1;
                        break;
                }

                // if no arrow keys were pressed, return
                if (xDirection == 0 && yDirection == 0) {
                    return super.keyPressed(keyCode, scanCode, modifiers);
                }

                // deal with modifiers here
                // https://www.glfw.org/docs/3.3/group__mods.html
                // shift keys
                if (MathUtils.isBitOn(modifiers, 0)) {
                    xDirection *= 10;
                    yDirection *= 10;
                }
                // ctrl keys
                if (MathUtils.isBitOn(modifiers, 1)) {
                    xDirection *= 5;
                    yDirection *= 5;
                }

                // update selected element position
                selectedElementX = coreSettings.x;
                selectedElementY = coreSettings.y;

                // update position in config
                updateElementPosition(xDirection, yDirection);
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        highlightedElementIndex = mouseInElement(mouseX, mouseY);

        List<Text> lines = new ArrayList<>(List.of(
                title,
                Text.literal("Click to select, hold click and drag to move"),
                Text.literal("Arrow keys for precise movement, Ctrl = x5, Shift = x10"),
                Text.literal("Space to cycle alignment"),
                Text.literal("Escape to close without saving"),
                Text.empty()
        ));

        // render backdrops
        // if both indices are the same, use highlighted
        // otherwise, render separately
        if (highlightedElementIndex != -1 && highlightedElementIndex == selectedElementIndex) {
            ElementCorners corners = RenderUtils.CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex).corners;
            context.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    0x7fffffff
            );
        } else {
            if (highlightedElementIndex != -1) {
                ElementCorners corners = RenderUtils.CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex).corners;
                context.fill(
                        corners.left,
                        corners.top,
                        corners.right,
                        corners.bottom,
                        0x7fffffff
                );
            }

            if (selectedElementIndex != -1) {
                ElementCorners corners = RenderUtils.CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).corners;
                context.fill(
                        corners.left,
                        corners.top,
                        corners.right,
                        corners.bottom,
                        0x7fffffff
                );
            }
        }

        // render element information
        if (highlightedElementIndex != -1) {
            CoreSettings coreSettings = RenderUtils.CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex).coreSettings;

            lines.add(Text.of(coreSettings.elementName));
            lines.add(Text.of("X: %d Y: %d".formatted(coreSettings.x, coreSettings.y)));
            lines.add(Text.of("Anchor: %s".formatted(coreSettings.elementAnchor.toString())));

            if (highlightedElementIndex == selectedElementIndex) {
                lines.add(Text.of("Highlighted & Selected"));
            } else {
                lines.add(Text.of("Highlighted"));
            }
        } else if (selectedElementIndex != -1) {
            CoreSettings coreSettings = RenderUtils.CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).coreSettings;

            lines.add(Text.of(coreSettings.elementName));
            lines.add(Text.of("X: %d Y: %d".formatted(coreSettings.x, coreSettings.y)));
            lines.add(Text.of("Anchor: %s".formatted(coreSettings.elementAnchor.toString())));
            lines.add(Text.of("Selected"));
        }

        for (int i = 0; i < lines.size(); i++) {
            context.drawCenteredTextWithShadow(textRenderer, lines.get(i), width / 2, 10 + (textRenderer.fontHeight + 2) * i, 0xffffff);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        selectedElementIndex = mouseInElement(mouseX, mouseY);

        if (selectedElementIndex != -1) {
            CoreSettings coreSettings = RenderUtils.CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).coreSettings;

            selectedElementX = coreSettings.x;
            selectedElementY = coreSettings.y;

            selectedMouseX = mouseX;
            selectedMouseY = mouseY;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selectedElementIndex != -1) {
            updateElementPosition((int) (selectedMouseX - mouseX), (int) (selectedMouseY - mouseY));
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
