package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.FoodHudConfig;
import io.github.brainage04.brainagehud.util.InventoryCounter;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

/** How many of each food the player carries, in the order they appear in the inventory. */
public class FoodHud implements BasicCoreHudElement<FoodHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return lines;

        boolean showSlots = getElementConfig().showSlotCounts;
        InventoryCounter.countBy(
                InventoryCounter.slots(player),
                stack -> stack.has(DataComponents.FOOD),
                ItemStack::getItem
        ).forEach((item, count) -> lines.add(InventoryCounter.format(new ItemStack(item).getHoverName().getString(), count, showSlots)));
        return lines;
    }

    @Override
    public FoodHudConfig getElementConfig() {
        return getConfig().foodHudConfig;
    }
}
