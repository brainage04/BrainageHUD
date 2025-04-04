package com.github.brainage04.brainagehud.hud.custom;

import com.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import com.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import com.github.brainage04.brainagehud.hud.core.CustomHudElement;
import com.github.brainage04.brainagehud.hud.core.HudElementEditor;
import com.github.brainage04.brainagehud.hud.core.HudRenderer;
import com.github.brainage04.brainagehud.config.core.ElementCorners;
import com.github.brainage04.brainagehud.util.MathUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ArmourInfoHud implements CustomHudElement<ArmourInfoHudConfig> {
    public static String generateItemInfo(ItemStack itemStack, ArmourInfoHudConfig settings) {
        String itemString = "";

        if (settings.showItemNames) {
            itemString += "%s".formatted(itemStack.getName().getString());
        }

        if (itemStack.getMaxDamage() > 0) {
            if (settings.showItemNames && settings.durabilityFormat != DurabilityFormat.NONE) {
                itemString += ": ";
            }

            int currentDurability = itemStack.getMaxDamage() - itemStack.getDamage();

            itemString += switch (settings.durabilityFormat) {
                case NONE -> "";
                case ONE_NUMBER -> "%d".formatted(currentDurability);
                case BOTH_NUMBERS -> "%d / %d".formatted(currentDurability, itemStack.getMaxDamage());
                case PERCENTAGE -> "%s%%".formatted(MathUtils.roundDecimalPlaces(((float) currentDurability) / itemStack.getMaxDamage() * 100, settings.durabilityDecimalPlaces));
            };
        }

        return itemString;
    }

    @Override
    public void render(TextRenderer renderer, DrawContext drawContext) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        List<ItemStack> itemStacksTemp = new ArrayList<>(List.of());
        List<ItemStack> itemStacks = new ArrayList<>(List.of());
        List<String> lines = new ArrayList<>(List.of());
        List<Integer> lineWidths = new ArrayList<>(List.of());

        if (getElementConfig().showOffHand) {
            itemStacksTemp.add(player.getOffHandStack());
        }

        if (getElementConfig().showMainHand) {
            itemStacksTemp.add(player.getMainHandStack());
        }

        if (getElementConfig().showArmour) {
            itemStacksTemp.add(player.getInventory().getArmorStack(3));
            itemStacksTemp.add(player.getInventory().getArmorStack(2));
            itemStacksTemp.add(player.getInventory().getArmorStack(1));
            itemStacksTemp.add(player.getInventory().getArmorStack(0));
        }

        for (ItemStack itemStack : itemStacksTemp) {
            if (itemStack.getItem() != Items.AIR) {
                itemStacks.add(itemStack);
            }
        }

        for (ItemStack itemStack : itemStacks) {
            lines.add(generateItemInfo(itemStack, getElementConfig()));
        }

        // custom rendering logic
        // determine bounds of HUD element
        // determine longest line of text (for element width)
        int elementWidth = 16 + getConfig().elementPadding * 2;
        int maxLineWidth = 0;
        for (String line : lines) {
            int currentLineWidth = renderer.getWidth(line);
            lineWidths.add(currentLineWidth);

            if (currentLineWidth > maxLineWidth) {
                maxLineWidth = currentLineWidth;
            }
        }
        elementWidth += maxLineWidth;
        // determine element height
        int elementHeight = 16 * itemStacks.size() + getConfig().elementPadding * (itemStacks.size() - 1);

        // horizontal adjustments (for element)
        int posX = switch (getElementConfig().coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getElementConfig().coreSettings.x + (HudRenderer.getScaledWidth() - elementWidth) - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> getElementConfig().coreSettings.x + (HudRenderer.getScaledWidth() - elementWidth) / 2;
            default -> getElementConfig().coreSettings.x + getConfig().elementPadding * 2;
        };
        // vertical adjustments
        int posY = HudRenderer.getPosY(getElementConfig().coreSettings, elementHeight);
        // additional adjustments
        switch (getElementConfig().coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= getConfig().elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= getConfig().elementPadding;
        }

        // adjust for padding
        ElementCorners corners = HudRenderer.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);
        corners.bottom += getConfig().elementPadding * 2;
        HudElementEditor.CORE_SETTINGS_ELEMENTS.get(getElementConfig().coreSettings.elementId).corners = corners;

        // render backdrop
        if (getConfig().backdropOpacity > 0) {
            drawContext.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    -1,
                    getConfig().backdropOpacity << 24
            );
        }

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);

            // horizontal adjustments (for line)
            int lineWidth = lineWidths.get(i);
            int linePosX = switch (getElementConfig().coreSettings.elementAnchor) { // do not ask how I figured this out SERIOUSLY
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> posX - (lineWidth - elementWidth + 16 + getConfig().elementPadding * 2);
                case TOP, CENTER, BOTTOM -> posX - (lineWidth - elementWidth + 16 + getConfig().elementPadding * 2) / 2;
                default -> posX;
            };
            int linePosY = posY + 5 + i * (16 + getConfig().elementPadding);

            drawContext.drawItem(itemStack, linePosX, posY + i * (16 + getConfig().elementPadding));
            if (getElementConfig().showDurabilityBar && itemStack.getMaxDamage() > 0 && itemStack.getDamage() > 0) {
                // taken from package net.minecraft.client.gui.InGameHud; line 615
                int a = itemStack.getItemBarStep();
                int b = itemStack.getItemBarColor();
                int c = linePosX + 2;
                int d = linePosY + 8;
                drawContext.fill(RenderLayer.getGuiOverlay(), c, d, c + 13, d + 2, Colors.BLACK);
                drawContext.fill(RenderLayer.getGuiOverlay(), c, d, c + a, d + 1, b | Colors.BLACK);
            }
            drawContext.drawText(renderer, lines.get(i), linePosX + (16 + getConfig().elementPadding * 2), linePosY, getConfig().primaryTextColour, getConfig().textShadows);
        }
    }

    @Override
    public ArmourInfoHudConfig getElementConfig() {
        return getConfig().armourInfoHudConfig;
    }
}
