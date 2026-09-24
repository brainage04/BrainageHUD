package io.github.brainage04.brainagehud.hud.custom;

import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ArmourInfoHud implements CoreHudElement<ArmourInfoHudConfig> {
    public static String generateItemInfo(ItemStack itemStack, ArmourInfoHudConfig settings) {
        String itemString = "";

        if (settings.showItemNames) {
            itemString += "%s".formatted(itemStack.getHoverName().getString());
        }

        if (itemStack.getMaxDamage() > 0) {
            if (settings.showItemNames && settings.durabilityFormat != DurabilityFormat.NONE) {
                itemString += ": ";
            }

            int currentDurability = itemStack.getMaxDamage() - itemStack.getDamageValue();

            itemString += switch (settings.durabilityFormat) {
                case NONE -> "";
                case FIRST_NUMBER -> "%d".formatted(currentDurability);
                case BOTH_NUMBERS -> "%d / %d".formatted(currentDurability, itemStack.getMaxDamage());
                case PERCENTAGE -> "%s%%".formatted(MathUtils.roundDecimalPlaces((double) currentDurability / itemStack.getMaxDamage() * 100, settings.durabilityDecimalPlaces));
            };
        }

        return itemString;
    }

    @Override
    public void render(GuiGraphicsExtractor drawContext, DeltaTracker renderTickCounter) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Font renderer = Minecraft.getInstance().font;
        ArmourInfoHudConfig config = getElementConfig();
        List<ItemInfo> items = new ArrayList<>(6);

        if (config.showOffHand) {
            addItem(items, player.getOffhandItem(), config, renderer);
        }
        if (config.showMainHand) {
            addItem(items, player.getMainHandItem(), config, renderer);
        }
        if (config.showArmour) {
            addItem(items, player.getItemBySlot(EquipmentSlot.HEAD), config, renderer);
            addItem(items, player.getItemBySlot(EquipmentSlot.CHEST), config, renderer);
            addItem(items, player.getItemBySlot(EquipmentSlot.LEGS), config, renderer);
            addItem(items, player.getItemBySlot(EquipmentSlot.FEET), config, renderer);
        }
        if (items.isEmpty()) return;

        int elementPadding = HudRendererLib.getPadding(config.coreSettings);
        int maxLineWidth = 0;
        for (ItemInfo item : items) {
            maxLineWidth = Math.max(maxLineWidth, item.textWidth());
        }

        int itemTextOffset = 16 + elementPadding * 2;
        int elementWidth = itemTextOffset + maxLineWidth;
        int elementHeight = 16 * items.size() + elementPadding * (items.size() - 1);
        CustomHudLayout.Origin origin = CustomHudLayout.place(drawContext, config.coreSettings, elementWidth, elementHeight);
        int posX = origin.x();
        int posY = origin.y();

        for (int i = 0; i < items.size(); i++) {
            ItemInfo item = items.get(i);
            int contentWidth = itemTextOffset + item.textWidth();
            int linePosX = HudRenderer.alignContentX(config.coreSettings, posX, elementWidth, contentWidth);
            int itemPosY = posY + i * (16 + elementPadding);
            int linePosY = itemPosY + 5;

            drawContext.item(item.stack(), linePosX, itemPosY);

            if (config.showDurabilityBar && item.stack().getMaxDamage() > 0 && item.stack().getDamageValue() > 0) {
                int barWidth = item.stack().getBarWidth();
                int barColour = item.stack().getBarColor();
                int barX = linePosX + 2;
                int barY = linePosY + 8;
                drawContext.fill(RenderPipelines.GUI, barX, barY, barX + 13, barY + 2, CommonColors.BLACK);
                drawContext.fill(RenderPipelines.GUI, barX, barY, barX + barWidth, barY + 1, barColour | CommonColors.BLACK);
            }

            drawContext.text(
                    renderer,
                    item.text(),
                    linePosX + itemTextOffset,
                    linePosY,
                    HudRendererLib.getTextColour(config.coreSettings),
                    HudRendererLib.getTextShadows(config.coreSettings)
            );
        }
    }

    private static void addItem(List<ItemInfo> items, ItemStack stack, ArmourInfoHudConfig config, Font renderer) {
        if (stack.isEmpty()) return;

        String text = generateItemInfo(stack, config);
        items.add(new ItemInfo(stack, text, renderer.width(text)));
    }

    private record ItemInfo(ItemStack stack, String text, int textWidth) {
    }

    @Override
    public ArmourInfoHudConfig getElementConfig() {
        return getConfig().armourInfoHudConfig;
    }
}
