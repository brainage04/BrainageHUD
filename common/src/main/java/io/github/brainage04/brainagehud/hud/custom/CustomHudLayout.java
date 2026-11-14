package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class CustomHudLayout {
    record Origin(int x, int y) {}

    private CustomHudLayout() {}

    static Origin place(
            GuiGraphicsExtractor drawContext,
            CoreSettings coreSettings,
            int contentWidth,
            int contentHeight) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);
        int posX = HudRenderer.getPosX(coreSettings, contentWidth);
        int posY = HudRenderer.getPosY(coreSettings, contentHeight);
        switch (coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= elementPadding;
        }

        ElementCorners corners =
                HudRenderer.getCornersWithPadding(
                        posX, posY, posX + contentWidth, posY + contentHeight, coreSettings);
        corners.bottom += elementPadding * 2;
        HudRenderer.setElementBounds(coreSettings, corners);
        HudRenderer.renderBackdrop(drawContext, corners, coreSettings);

        return new Origin(posX, posY);
    }

    /**
     * Clears the editor bounds of an element that draws nothing this frame, as {@link
     * HudRenderer#renderElement} does for empty text, so the editor drops the element.
     */
    static void clear(CoreSettings coreSettings) {
        HudRenderer.setElementBounds(coreSettings, -1, -1, -1, -1);
    }
}
