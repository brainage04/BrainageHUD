package com.github.brainage04.brainagehud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.hud.HUDRenderer;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

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

    private void saveElementSettings() {
        getConfig().positionHudConfig.coreSettings = elementList.get(0);
        getConfig().dateTimeHudConfig.coreSettings = elementList.get(1);
        getConfig().toggleSprintHudConfig.coreSettings = elementList.get(2);
        getConfig().performanceHudConfig.coreSettings = elementList.get(3);
        getConfig().networkHudConfig.coreSettings = elementList.get(4);
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

    @Override
    public void close() {
        saveElementSettings();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        highlightedElementIndex = mouseInElement(mouseX, mouseY);

        if (highlightedElementIndex != -1) {
            context.fill(
                    RenderUtils.elementCorners.get(highlightedElementIndex)[0],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[1],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[2],
                    RenderUtils.elementCorners.get(highlightedElementIndex)[3],
                    getConfig().highlightedTextColour
            );

            List<String> lines = new ArrayList<>(Arrays.asList(
                    String.format(
                            Locale.ROOT,
                            "%s: %s",
                            BrainageHUD.MOD_NAME,
                            elementList.get(highlightedElementIndex).elementName
                    ),
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

            HUDRenderer.renderElement(
                    MinecraftClient.getInstance().textRenderer,
                    context,
                    lines,
                    0,
                    10,
                    BrainageHUDConfig.ElementAnchor.TOP
            );
        }

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

            /*
            int posX = switch (coreSettings.elementAnchor) {
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> scaledWidth - coreSettings.x - lineWidth;
                case TOP, CENTER, BOTTOM -> (scaledWidth - coreSettings.x - lineWidth) / 2;
                default -> coreSettings.x;
            };
            int posY = switch (coreSettings.elementAnchor) {
                case BOTTOMLEFT, BOTTOM, BOTTOMRIGHT -> scaledHeight - coreSettings.y - renderer.fontHeight;
                case LEFT, CENTER, RIGHT -> (scaledHeight - coreSettings.y - renderer.fontHeight) / 2;
                default -> coreSettings.y;
            };
             */

            elementList.get(selectedElementIndex).x = (int) (MathHelper.clamp(selectedElementX - diffX, 0, MinecraftClient.getInstance().getWindow().getScaledWidth()));
            elementList.get(selectedElementIndex).y = (int) (MathHelper.clamp(selectedElementY - diffY, 0, MinecraftClient.getInstance().getWindow().getScaledHeight()));
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        selectedElementIndex = -1;

        return super.mouseReleased(mouseX, mouseY, button);
    }
}
