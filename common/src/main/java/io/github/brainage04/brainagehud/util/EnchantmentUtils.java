package io.github.brainage04.brainagehud.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Ported from GetEnchantInfo's {@code io.github.brainage04.util.EnchantmentUtils}. */
public class EnchantmentUtils {
    /** The enchantment at its maximum level, followed by the level already on {@code itemStack}. */
    public static Component getEnchantmentName(Holder<Enchantment> enchantmentHolder, ItemStack itemStack) {
        int maxLevel = enchantmentHolder.value().getMaxLevel();
        Component enchantmentName = Enchantment.getFullname(enchantmentHolder, maxLevel);
        int currentLevel = itemStack.getEnchantments().getLevel(enchantmentHolder);

        if (currentLevel <= 0) return enchantmentName;

        if (currentLevel == maxLevel) {
            return Component.empty().append("You already have ").append(enchantmentName);
        }

        return Component.empty()
                .append(enchantmentName)
                .append(" - you have ")
                .append(Enchantment.getFullname(enchantmentHolder, currentLevel));
    }

    public static MutableComponent getEnchantmentName(Holder<Enchantment> enchantmentHolder) {
        ChatFormatting formatting =
                enchantmentHolder.is(EnchantmentTags.CURSE)
                        ? ChatFormatting.RED
                        : ChatFormatting.GRAY;

        return enchantmentHolder.value().description().copy().withStyle(formatting);
    }

    public static Component joinEnchantmentNames(
            List<? extends Holder<Enchantment>> enchantments, ItemStack itemStack) {
        MutableComponent text = Component.empty();

        for (int i = 0; i < enchantments.size(); i++) {
            if (i > 0) text.append(", ");

            text.append(getEnchantmentName(enchantments.get(i), itemStack));
        }

        return text;
    }

    /** The identifier used by the configured enchantment blacklist. */
    public static String getEnchantmentId(Holder<Enchantment> enchantmentHolder) {
        return enchantmentHolder
                .unwrapKey()
                .map(key -> key.identifier().toString())
                .orElseGet(enchantmentHolder::getRegisteredName);
    }

    /**
     * The enchantments that could still be added to {@code itemStack}: applicable to the item, not
     * already present, not blacklisted, and compatible with every enchantment already on the item.
     */
    public static List<Holder.Reference<Enchantment>> getMissingEnchantments(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            ItemStack itemStack,
            ItemEnchantments presentEnchantments,
            Collection<String> blacklistedEnchantmentIds) {
        List<Holder.Reference<Enchantment>> missingEnchantments = new ArrayList<>();
        Set<Holder<Enchantment>> presentHolders = presentEnchantments.keySet();

        enchantmentRegistry
                .listElements()
                .forEach(
                        enchantmentHolder -> {
                            if (blacklistedEnchantmentIds.contains(
                                    getEnchantmentId(enchantmentHolder))) return;

                            if (presentEnchantments.getLevel(enchantmentHolder) > 0) return;

                            if (!enchantmentHolder.value().canEnchant(itemStack)) return;

                            for (Holder<Enchantment> presentHolder : presentHolders) {
                                if (!Enchantment.areCompatible(presentHolder, enchantmentHolder))
                                    return;
                            }

                            missingEnchantments.add(enchantmentHolder);
                        });

        return missingEnchantments;
    }

    /**
     * Groups enchantments that cannot coexist, so that each group can be displayed as a single
     * "choose one" line. Every enchantment that conflicts with no other enchantment is returned as
     * a group of one, and the input order is preserved by returning {@link java.util.List}s rather
     * than {@link Set}s.
     */
    public static List<List<Holder.Reference<Enchantment>>> groupConflictingEnchantments(
            List<Holder.Reference<Enchantment>> enchantments) {
        List<List<Holder.Reference<Enchantment>>> conflictGroups = new ArrayList<>();
        int size = enchantments.size();
        boolean[] grouped = new boolean[size];

        for (int i = 0; i < size; i++) {
            if (grouped[i]) continue;

            List<Holder.Reference<Enchantment>> group = new ArrayList<>();
            Deque<Integer> queue = new ArrayDeque<>();
            grouped[i] = true;
            queue.addLast(i);

            while (!queue.isEmpty()) {
                Holder.Reference<Enchantment> currentEnchantment =
                        enchantments.get(queue.removeFirst());
                group.add(currentEnchantment);

                for (int j = 0; j < size; j++) {
                    if (grouped[j]) continue;

                    if (Enchantment.areCompatible(currentEnchantment, enchantments.get(j)))
                        continue;

                    grouped[j] = true;
                    queue.addLast(j);
                }
            }

            conflictGroups.add(group);
        }

        return conflictGroups;
    }
}
