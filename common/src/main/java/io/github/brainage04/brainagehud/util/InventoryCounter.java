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

public final class InventoryCounter {
    private InventoryCounter() {}

    public record Count(int total, List<Integer> perSlot) {
        static final Count NONE = new Count(0, List.of());

        Count plus(int amount) {
            List<Integer> slots = new ArrayList<>(perSlot);
            slots.add(amount);
            return new Count(total + amount, slots);
        }
    }

    public static List<ItemStack> slots(LocalPlayer player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> slots = new ArrayList<>(Inventory.SLOT_OFFHAND + 1);
        for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++) {
            slots.add(inventory.getItem(slot));
        }
        return slots;
    }

    public static Count count(List<ItemStack> slots, Predicate<ItemStack> matches) {
        Count count = Count.NONE;
        for (ItemStack stack : slots) {
            if (!stack.isEmpty() && matches.test(stack)) count = count.plus(stack.getCount());
        }
        return count;
    }

    public static <K> Map<K, Count> countBy(
            List<ItemStack> slots, Predicate<ItemStack> matches, Function<ItemStack, K> key) {
        Map<K, Count> counts = new LinkedHashMap<>();
        for (ItemStack stack : slots) {
            if (!stack.isEmpty() && matches.test(stack)) {
                counts.merge(
                        key.apply(stack),
                        Count.NONE.plus(stack.getCount()),
                        (existing, added) -> existing.plus(added.total()));
            }
        }
        return counts;
    }

    public static Component header(String title, boolean none) {
        MutableComponent header =
                Component.empty().append(Component.literal(title).withStyle(ChatFormatting.BOLD));
        if (none) header.append(Component.literal(" N/A").withStyle(ChatFormatting.RED));
        return header;
    }

    public static String format(String label, Count count, boolean showSlots) {
        String line = "%s: %d".formatted(label, count.total());
        if (showSlots && count.perSlot().size() > 1) line += " " + count.perSlot();
        return line;
    }
}
