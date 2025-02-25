package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.ModConfig;
import com.github.brainage04.brainagehud.util.MathUtils;
import com.github.brainage04.brainagehud.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ArmourInfoHUD {
    public static String generateItemInfo(ItemStack itemStack, ModConfig.ArmourInfoHUDConfig settings) {
        String itemString = "";

        if (settings.showItemNames) {
            itemString += "%s".formatted(itemStack.getName().getString());
        }

        if (itemStack.getMaxDamage() > 0) {
            if (settings.showItemNames && settings.durabilityFormat != ModConfig.DurabilityFormat.NONE) {
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

    public static void armourInfoHud(TextRenderer renderer, DrawContext drawContext, ModConfig.ArmourInfoHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        List<ItemStack> itemStacksTemp = new ArrayList<>(List.of());
        List<ItemStack> itemStacks = new ArrayList<>(List.of());
        List<String> lines = new ArrayList<>(List.of());
        List<Integer> lineWidths = new ArrayList<>(List.of());

        assert MinecraftClient.getInstance().player != null;

        if (settings.showOffHand) {
            itemStacksTemp.add(MinecraftClient.getInstance().player.getOffHandStack());
        }

        if (settings.showMainHand) {
            itemStacksTemp.add(MinecraftClient.getInstance().player.getMainHandStack());
        }

        if (settings.showArmour) {
            itemStacksTemp.add(MinecraftClient.getInstance().player.getInventory().getArmorStack(3));
            itemStacksTemp.add(MinecraftClient.getInstance().player.getInventory().getArmorStack(2));
            itemStacksTemp.add(MinecraftClient.getInstance().player.getInventory().getArmorStack(1));
            itemStacksTemp.add(MinecraftClient.getInstance().player.getInventory().getArmorStack(0));
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

        // adjust for padding
        int[] corners = RenderUtils.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight);
        corners[3] += getConfig().elementPadding * 2;

        // store corners for use in HUDElementEditor
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[0] = corners[0];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[1] = corners[1];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[2] = corners[2];
        RenderUtils.elementCorners.get(settings.coreSettings.elementId)[3] = corners[3];

        // render backdrop
        if (getConfig().backdropOpacity > 0) {
            drawContext.fill(
                    corners[0],
                    corners[1],
                    corners[2],
                    corners[3],
                    -1,
                    getConfig().backdropOpacity << 24
            );
        }

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);

            // horizontal adjustments (for line)
            int lineWidth = lineWidths.get(i);
            int linePosX = switch (settings.coreSettings.elementAnchor) { // do not ask how I figured this out SERIOUSLY
                case TOPRIGHT, RIGHT, BOTTOMRIGHT -> posX - (lineWidth - elementWidth + 16 + getConfig().elementPadding * 2);
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
