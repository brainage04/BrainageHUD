package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Placement shared by the HUD elements that draw their own content instead of text lines. */
final class CustomHudLayout {
    record Origin(int x, int y) {}

    private CustomHudLayout() {}

    /**
     * Positions content of the given size according to {@code coreSettings}, registers its padded
     * bounds with the element editor and draws the backdrop behind it.
     *
     * @return the top-left corner at which the content should be drawn
     */
    static Origin place(GuiGraphicsExtractor drawContext, CoreSettings coreSettings, int contentWidth, int contentHeight) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);
        int posX = HudRenderer.getPosX(coreSettings, contentWidth);
        int posY = HudRenderer.getPosY(coreSettings, contentHeight);
        switch (coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= elementPadding;
        }

        ElementCorners corners = HudRenderer.getCornersWithPadding(
                posX,
                posY,
                posX + contentWidth,
                posY + contentHeight,
                coreSettings
        );
        corners.bottom += elementPadding * 2;
        HudRenderer.setElementBounds(coreSettings, corners);
        HudRenderer.renderBackdrop(drawContext, corners, coreSettings);

        return new Origin(posX, posY);
    }
}
