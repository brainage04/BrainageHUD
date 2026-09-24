package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ProjectileHudConfig;
import io.github.brainage04.brainagehud.util.InventoryCounter;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

/** How many of each throwable or shootable item the player carries. Kinds they have none of are left out. */
public class ProjectileHud implements BasicCoreHudElement<ProjectileHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return lines;

        ProjectileHudConfig config = getElementConfig();
        List<ItemStack> slots = InventoryCounter.slots(player);
        add(lines, slots, config, config.showArrows, "Arrows", stack -> stack.is(ItemTags.ARROWS));
        add(lines, slots, config, config.showSnowballs, "Snowballs", stack -> stack.is(Items.SNOWBALL));
        add(lines, slots, config, config.showEggs, "Eggs", stack -> stack.is(ItemTags.EGGS));
        add(lines, slots, config, config.showEnderPearls, "Ender Pearls", stack -> stack.is(Items.ENDER_PEARL));
        add(lines, slots, config, config.showWindCharges, "Wind Charges", stack -> stack.is(Items.WIND_CHARGE));
        return lines;
    }

    private static void add(TextList lines, List<ItemStack> slots, ProjectileHudConfig config, boolean shown, String label, Predicate<ItemStack> matches) {
        if (!shown) return;
        InventoryCounter.Count count = InventoryCounter.count(slots, matches);
        if (count.total() > 0) lines.add(InventoryCounter.format(label, count, config.showSlotCounts));
    }

    @Override
    public ProjectileHudConfig getElementConfig() {
        return getConfig().projectileHudConfig;
    }
}
