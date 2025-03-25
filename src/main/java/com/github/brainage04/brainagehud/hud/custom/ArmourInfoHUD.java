package com.github.brainage04.brainagehud.hud.custom;

import com.github.brainage04.brainagehud.config.core.ModConfig;
import com.github.brainage04.brainagehud.config.hud.custom.ArmourInfoHUDConfig;
import com.github.brainage04.brainagehud.config.hud.custom.DurabilityFormat;
import com.github.brainage04.brainagehud.hud.core.HUDRenderer;
import com.github.brainage04.brainagehud.util.ElementCorners;
import com.github.brainage04.brainagehud.util.MathUtils;
import com.github.brainage04.brainagehud.util.RenderUtils;
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

public class ArmourInfoHUD {
    public static String generateItemInfo(ItemStack itemStack, ArmourInfoHUDConfig settings) {
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

    public static void render(TextRenderer renderer, DrawContext drawContext, ArmourInfoHUDConfig settings) {
        if (!settings.coreSettings.enabled) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        List<ItemStack> itemStacksTemp = new ArrayList<>(List.of());
        List<ItemStack> itemStacks = new ArrayList<>(List.of());
        List<String> lines = new ArrayList<>(List.of());
        List<Integer> lineWidths = new ArrayList<>(List.of());

        if (settings.showOffHand) {
            itemStacksTemp.add(player.getOffHandStack());
        }

        if (settings.showMainHand) {
            itemStacksTemp.add(player.getMainHandStack());
        }

        if (settings.showArmour) {
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
            lines.add(generateItemInfo(itemStack, settings));
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
        int posX = switch (settings.coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> settings.coreSettings.x + (RenderUtils.getScaledWidth() - elementWidth) - getConfig().elementPadding * 2;
            case TOP, CENTER, BOTTOM -> settings.coreSettings.x + (RenderUtils.getScaledWidth() - elementWidth) / 2;
            default -> settings.coreSettings.x + getConfig().elementPadding * 2;
        };
        // vertical adjustments
        int posY = HUDRenderer.getPosY(settings.coreSettings, elementHeight);
        // additional adjustments
        switch (settings.coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> posY -= getConfig().elementPadding * 2;
            case LEFT, CENTER, RIGHT -> posY -= getConfig().elementPadding;
        }

        // adjust for padding
        ElementCorners corners = RenderUtils.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);
        corners.bottom += getConfig().elementPadding * 2;
        RenderUtils.CORE_SETTINGS_ELEMENTS.get(settings.coreSettings.elementId).corners = corners;

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
            int linePosX = switch (settings.coreSettings.elementAnchor) { // do not ask how I figured this out SERIOUSLY
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> posX - (lineWidth - elementWidth + 16 + getConfig().elementPadding * 2);
                case TOP, CENTER, BOTTOM -> posX - (lineWidth - elementWidth + 16 + getConfig().elementPadding * 2) / 2;
                default -> posX;
            };
            int linePosY = posY + 5 + i * (16 + getConfig().elementPadding);

            drawContext.drawItem(itemStack, linePosX, posY + i * (16 + getConfig().elementPadding));
            if (settings.showDurabilityBar && itemStack.getMaxDamage() > 0 && itemStack.getDamage() > 0) {
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
}
