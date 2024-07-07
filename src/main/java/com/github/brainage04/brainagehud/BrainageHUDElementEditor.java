package com.github.brainage04.brainagehud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.hud.HUDRenderer;
import com.github.brainage04.brainagehud.util.MathUtils;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.github.brainage04.brainagehud.util.ConfigUtils.*;

public class BrainageHUDElementEditor extends Screen {
    public List<BrainageHUDConfig.CoreSettings> elementList;

    public int selectedElementIndex = -1;

    public double selectedElementX = -1;
    public double selectedElementY = -1;

    public double selectedMouseX = -1;
    public double selectedMouseY = -1;

    public int highlightedElementIndex = -1;

    public BrainageHUDElementEditor() {
        super(Text.of(BrainageHUD.MOD_NAME + " Element Editor"));

        this.elementList = loadElementSettings();

        // refresh element corners
        RenderUtils.elementCorners.clear();
        while (RenderUtils.elementCorners.size() < loadElementSettings().size()) {
            RenderUtils.elementCorners.add(new int[]{-1, -1, -1, -1});
        }
    }

    private List<BrainageHUDConfig.CoreSettings> loadElementSettings() {
        return new ArrayList<>(Arrays.asList(
                getConfig().positionHudConfig.coreSettings,
                getConfig().dateTimeHudConfig.coreSettings,
                getConfig().toggleSprintHudConfig.coreSettings,
                getConfig().performanceHudConfig.coreSettings,
                getConfig().networkHudConfig.coreSettings
        ));
    }

    public int mouseInElement(double mouseX, double mouseY) {
        for (int i = 0; i < elementList.size(); i++) {
            int[] corners = RenderUtils.elementCorners.get(i);

            if (RenderUtils.mouseInRect(
                    corners[0],
                    corners[1],
                    corners[2],
                    corners[3],
                    mouseX,
                    mouseY
            )) {
                return i;
            }
        }

        return -1;
    }

    public ButtonWidget button1 = ButtonWidget.builder(Text.literal("Undo & Close"), button -> {
                loadConfig();
                super.close();
            })
                .dimensions(MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 - 210, MinecraftClient.getInstance().getWindow().getScaledHeight() - 40, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Reverts the current positions to what they were before and closes the screen.")))
                .build();
    public ButtonWidget button2 = ButtonWidget.builder(Text.literal("Save & Close"), button -> this.close())
                .dimensions(MinecraftClient.getInstance().getWindow().getScaledWidth() / 2 + 10, MinecraftClient.getInstance().getWindow().getScaledHeight() - 40, 200, 20)
                .tooltip(Tooltip.of(Text.literal("Saves the current positions and closes the screen.")))
                .build();

    public void updateElementPosition(int deltaX, int deltaY) {
        elementList.get(selectedElementIndex).x = (int) (MathHelper.clamp(selectedElementX - deltaX, getConfig().screenMargin, MinecraftClient.getInstance().getWindow().getScaledWidth() - getConfig().screenMargin));
        elementList.get(selectedElementIndex).y = (int) (MathHelper.clamp(selectedElementY - deltaY, getConfig().screenMargin, MinecraftClient.getInstance().getWindow().getScaledHeight() - getConfig().screenMargin));
    }

    @Override
    protected void init() {
        addDrawableChild(button1);
        addDrawableChild(button2);
    }

    @Override
    public void close() {
        saveConfig();
        super.close();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (selectedElementIndex != -1) {
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
            selectedElementX = elementList.get(selectedElementIndex).x;
            selectedElementY = elementList.get(selectedElementIndex).y;

            // update position in config
            updateElementPosition(xDirection, yDirection);
        }

        //return super.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // render title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xffffff);

        highlightedElementIndex = mouseInElement(mouseX, mouseY);

        List<String> lines = new ArrayList<>(List.of());

        // render backdrops
        // if both indices are the same, use highlighted
        // otherwise, render separately
        if (highlightedElementIndex != -1 && highlightedElementIndex == selectedElementIndex) {
            context.fill(
                    RenderUtils.elementCorners.get(highlightedElementIndex)[0],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[1],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[2],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[3],
                    getConfig().highlightedElementColour
            );
        } else {
            if (highlightedElementIndex != -1) {
                context.fill(
                        RenderUtils.elementCorners.get(highlightedElementIndex)[0],
                        RenderUtils.elementCorners.get(highlightedElementIndex)[1],
                        RenderUtils.elementCorners.get(highlightedElementIndex)[2],
                        RenderUtils.elementCorners.get(highlightedElementIndex)[3],
                        getConfig().highlightedElementColour
                );
            }

            if (selectedElementIndex != -1) {
                context.fill(
                        RenderUtils.elementCorners.get(selectedElementIndex)[0],
                        RenderUtils.elementCorners.get(selectedElementIndex)[1],
                        RenderUtils.elementCorners.get(selectedElementIndex)[2],
                        RenderUtils.elementCorners.get(selectedElementIndex)[3],
                        getConfig().highlightedElementColour
                );
            }
        }

        // render element information
        if (highlightedElementIndex != -1) {
            lines = new ArrayList<>(Arrays.asList(
                    elementList.get(highlightedElementIndex).elementName,
                    String.format(
                            Locale.ROOT,
                            "X: %d Y: %d",
                            elementList.get(highlightedElementIndex).x,
                            elementList.get(highlightedElementIndex).y
                    ),
                    String.format(
                            Locale.ROOT,
                            "Anchor: %s",
                            elementList.get(highlightedElementIndex).elementAnchor.name()
                    )
            ));

            if (highlightedElementIndex == selectedElementIndex) {
                lines.add("Highlighted & Selected");
            } else {
                lines.add("Highlighted");
            }
        } else if (selectedElementIndex != -1) {
            lines = new ArrayList<>(Arrays.asList(
                    elementList.get(selectedElementIndex).elementName,
                    String.format(
                            Locale.ROOT,
                            "X: %d Y: %d",
                            elementList.get(selectedElementIndex).x,
                            elementList.get(selectedElementIndex).y
                    ),
                    String.format(
                            Locale.ROOT,
                            "Anchor: %s",
                            elementList.get(selectedElementIndex).elementAnchor.name()
                    ),
                    "Selected"
            ));
        }

        HUDRenderer.renderElement(
                MinecraftClient.getInstance().textRenderer,
                context,
                lines,
                0,
                (textRenderer.fontHeight + getConfig().elementPadding) * 2,
                BrainageHUDConfig.ElementAnchor.TOP
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        selectedElementIndex = mouseInElement(mouseX, mouseY);

        if (selectedElementIndex != -1) {
            selectedElementX = elementList.get(selectedElementIndex).x;
            selectedElementY = elementList.get(selectedElementIndex).y;

            selectedMouseX = mouseX;
            selectedMouseY = mouseY;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selectedElementIndex != -1) {
            double diffX = switch (elementList.get(selectedElementIndex).elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> -(selectedMouseX - mouseX);
                case TOP, CENTER, BOTTOM -> -(selectedMouseX - mouseX) / 2;
                default -> selectedMouseX - mouseX;
            };
            double diffY = switch (elementList.get(selectedElementIndex).elementAnchor) {
                case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> -(selectedMouseY - mouseY);
                case LEFT, CENTER, RIGHT -> -(selectedMouseY - mouseY) / 2;
                default -> selectedMouseY - mouseY;
            };

            updateElementPosition((int) diffX, (int) diffY);
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
}
