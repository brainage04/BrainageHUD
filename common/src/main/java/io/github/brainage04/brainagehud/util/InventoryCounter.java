package io.github.brainage04.brainagehud.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Counts items across the player's inventory for the inventory tracker HUDs. */
public final class InventoryCounter {
    private InventoryCounter() {}

    /** How many matching items there are in total, and in each slot that holds some. */
    public record Count(int total, List<Integer> perSlot) {
        static final Count NONE = new Count(0, List.of());

        Count plus(int amount) {
            List<Integer> slots = new ArrayList<>(perSlot);
            slots.add(amount);
            return new Count(total + amount, slots);
        }
    }

    /** The hotbar from left to right, then the three inventory rows, then the off hand. */
    public static List<ItemStack> slots(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> slots = new ArrayList<>(Inventory.INVENTORY_SIZE + 1);
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            slots.add(inventory.getItem(slot));
        }
        slots.add(inventory.getItem(Inventory.SLOT_OFFHAND));
        return slots;
    }

    public static Count count(List<ItemStack> slots, Predicate<ItemStack> matches) {
        Count count = Count.NONE;
        for (ItemStack stack : slots) {
            if (!stack.isEmpty() && matches.test(stack)) count = count.plus(stack.getCount());
        }
        return count;
    }

    /**
     * Counts every matching item grouped by {@code key}, in the order each group is first found in
     * the slots.
     */
    public static <K> Map<K, Count> countBy(List<ItemStack> slots, Predicate<ItemStack> matches, Function<ItemStack, K> key) {
        Map<K, Count> counts = new LinkedHashMap<>();
        for (ItemStack stack : slots) {
            if (!stack.isEmpty() && matches.test(stack)) {
                counts.merge(key.apply(stack), Count.NONE.plus(stack.getCount()),
                        (existing, added) -> existing.plus(added.total()));
            }
        }
        return counts;
    }

    /** A tracker's bold header, e.g. "Food:", followed by a red "N/A" when the player carries none. */
    public static Component header(String title, boolean none) {
        MutableComponent header = Component.empty().append(Component.literal(title).withStyle(ChatFormatting.BOLD));
        if (none) header.append(Component.literal(" N/A").withStyle(ChatFormatting.RED));
        return header;
    }

    /** "Arrows: 96", or "Arrows: 96 [64, 32]" when the items are split over several slots. */
    public static String format(String label, Count count, boolean showSlots) {
        String line = "%s: %d".formatted(label, count.total());
        if (showSlots && count.perSlot().size() > 1) line += " " + count.perSlot();
        return line;
    }
}
