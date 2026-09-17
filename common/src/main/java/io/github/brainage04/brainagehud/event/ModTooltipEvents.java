package io.github.brainage04.brainagehud.event;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.Set;

/**
 * Ported from GetEnchantInfo's tooltip callback, which highlights maxed enchantment tooltip lines
 * in bold.
 */
public class ModTooltipEvents {
    public static void onItemTooltip(ItemStack itemStack, List<Component> lines) {
        if (!getConfig().enchantInfoConfig.highlightMaxLevelEnchants) return;

        boolean enchantedBook = itemStack.getItem() == Items.ENCHANTED_BOOK;
        Set<Holder<Enchantment>> enchantments;

        if (enchantedBook) {
            ItemEnchantments stored =
                    itemStack.getComponents().get(DataComponents.STORED_ENCHANTMENTS);
            if (stored == null) return;

            enchantments = stored.keySet();
        } else {
            enchantments = itemStack.getEnchantments().keySet();
        }

        for (int i = 0; i < lines.size(); i++) {
            for (Holder<Enchantment> enchantmentHolder : enchantments) {
                int level =
                        enchantedBook
                                ? itemStack
                                        .getOrDefault(
                                                DataComponents.STORED_ENCHANTMENTS,
                                                ItemEnchantments.EMPTY)
                                        .getLevel(enchantmentHolder)
                                : EnchantmentHelper.getItemEnchantmentLevel(
                                        enchantmentHolder, itemStack);

                if (lines.get(i)
                                .getString()
                                .startsWith(
                                        Enchantment.getFullname(enchantmentHolder, level)
                                                .getString())
                        && level == enchantmentHolder.value().getMaxLevel()) {
                    lines.set(i, lines.get(i).copy().withStyle(ChatFormatting.BOLD));
                }
            }
        }
    }
}
