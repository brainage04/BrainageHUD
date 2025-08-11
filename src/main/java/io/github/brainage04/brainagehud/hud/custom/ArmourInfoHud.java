package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsElement;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Colors;

import java.util.ArrayList;
import java.util.List;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ArmourInfoHud implements CoreHudElement<ArmourInfoHudConfig> {
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
                case FIRST_NUMBER -> "%d".formatted(currentDurability);
                case BOTH_NUMBERS -> "%d / %d".formatted(currentDurability, itemStack.getMaxDamage());
                case PERCENTAGE -> "%s%%".formatted(MathUtils.roundDecimalPlaces(((float) currentDurability) / itemStack.getMaxDamage() * 100, settings.durabilityDecimalPlaces));
            };
        }

        return itemString;
    }

    @Override
    public void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
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
            PlayerInventory inventory = player.getInventory();

            itemStacksTemp.add(inventory.getStack(EquipmentSlot.HEAD.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE)));
            itemStacksTemp.add(inventory.getStack(EquipmentSlot.CHEST.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE)));
            itemStacksTemp.add(inventory.getStack(EquipmentSlot.LEGS.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE)));
            itemStacksTemp.add(inventory.getStack(EquipmentSlot.FEET.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE)));
        }

        for (ItemStack itemStack : itemStacksTemp) {
            if (itemStack.getItem() != Items.AIR) {
                itemStacks.add(itemStack);
            }
        }

        for (ItemStack itemStack : itemStacks) {
            lines.add(generateItemInfo(itemStack, getElementConfig()));
        }

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        // custom rendering logic
        // determine bounds of HUD element
        // determine longest line of text (for element width)
        int elementPadding = HudRendererLib.getPadding(getElementConfig().coreSettings);

        int elementWidth = 16 + elementPadding * 2;
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
        int elementHeight = 16 * itemStacks.size() + elementPadding * (itemStacks.size() - 1);

        // horizontal adjustments (for element)
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

        // adjust for padding
        ElementCorners corners = HudRenderer.getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight, getElementConfig().coreSettings);
        corners.bottom += elementPadding * 2;

        CoreSettingsElement coreSettingsElement = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(getElementConfig().coreSettings.elementId);
        if (coreSettingsElement == null) {
            HudRendererLib.LOGGER.error("Core settings element with element ID {} in HudElementEditor.CORE_SETTINGS_ELEMENTS does not exist - this shouldn't happen!", getElementConfig().coreSettings.elementId);
        } else {
            coreSettingsElement.corners = corners;
            HudElementEditor.CORE_SETTINGS_ELEMENTS.put(getElementConfig().coreSettings.elementId, coreSettingsElement);
        }

        // render backdrop
        int backdropOpacity = HudRendererLib.getOpacity(getElementConfig().coreSettings);
        if (backdropOpacity > 0) {
            drawContext.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    backdropOpacity << 24
            );
        }

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);

            // horizontal adjustments (for line)
            int lineWidth = lineWidths.get(i);
            int linePosX = switch (getElementConfig().coreSettings.elementAnchor) { // do not ask how I figured this out SERIOUSLY
                case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> posX - (lineWidth - elementWidth + 16 + elementPadding * 2);
                case TOP, CENTER, BOTTOM -> posX - (lineWidth - elementWidth + 16 + elementPadding * 2) / 2;
                default -> posX;
            };
            int linePosY = posY + 5 + i * (16 + elementPadding);

            drawContext.drawItem(
                    itemStack,
                    linePosX,
                    posY + i * (16 + elementPadding)
            );

            if (getElementConfig().showDurabilityBar && itemStack.getMaxDamage() > 0 && itemStack.getDamage() > 0) {
                // taken from package net.minecraft.client.gui.InGameHud; line 615
                int a = itemStack.getItemBarStep();
                int b = itemStack.getItemBarColor();
                int c = linePosX + 2;
                int d = linePosY + 8;
                drawContext.fill(RenderPipelines.GUI, c, d, c + 13, d + 2, Colors.BLACK);
                drawContext.fill(RenderPipelines.GUI, c, d, c + a, d + 1, b | Colors.BLACK);
            }

            drawContext.drawText(
                    renderer,
                    lines.get(i),
                    linePosX + (16 + elementPadding * 2),
                    linePosY,
                    HudRendererLib.getTextColour(getElementConfig().coreSettings),
                    HudRendererLib.getTextShadows(getElementConfig().coreSettings)
            );
        }
    }

    @Override
    public ArmourInfoHudConfig getElementConfig() {
        return getConfig().armourInfoHudConfig;
    }
}
