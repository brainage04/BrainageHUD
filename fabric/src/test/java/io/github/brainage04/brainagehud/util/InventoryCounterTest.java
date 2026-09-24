package io.github.brainage04.brainagehud.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class InventoryCounterTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void countsEveryMatchingSlotInSlotOrder() {
        List<ItemStack> slots = List.of(
                new ItemStack(Items.ARROW, 32),
                ItemStack.EMPTY,
                new ItemStack(Items.BREAD, 5),
                new ItemStack(Items.ARROW, 64));

        InventoryCounter.Count arrows = InventoryCounter.count(slots, stack -> stack.is(Items.ARROW));

        assertEquals(96, arrows.total());
        assertEquals("Arrows: 96 [32, 64]", InventoryCounter.format("Arrows", arrows, true));
        assertEquals("Arrows: 96", InventoryCounter.format("Arrows", arrows, false));
    }

    @Test
    void listsNoSlotBreakdownForASingleStack() {
        InventoryCounter.Count pearls = InventoryCounter.count(
                List.of(new ItemStack(Items.ENDER_PEARL, 16)), stack -> stack.is(Items.ENDER_PEARL));

        assertEquals("Ender Pearls: 16", InventoryCounter.format("Ender Pearls", pearls, true));
    }

    @Test
    void groupsByKeyInTheOrderEachIsFirstFound() {
        List<ItemStack> slots = List.of(
                new ItemStack(Items.BREAD, 3),
                new ItemStack(Items.STONE, 64),
                new ItemStack(Items.APPLE, 2),
                new ItemStack(Items.BREAD, 10));

        Map<Item, InventoryCounter.Count> food = InventoryCounter.countBy(
                slots, stack -> !stack.is(Items.STONE), ItemStack::getItem);

        assertEquals(List.of(Items.BREAD, Items.APPLE), List.copyOf(food.keySet()));
        assertEquals(new InventoryCounter.Count(13, List.of(3, 10)), food.get(Items.BREAD));
        assertEquals(new InventoryCounter.Count(2, List.of(2)), food.get(Items.APPLE));
    }
}
