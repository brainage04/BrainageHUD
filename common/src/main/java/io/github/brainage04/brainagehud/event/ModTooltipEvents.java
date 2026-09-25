package io.github.brainage04.brainagehud.event;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Ported from GetEnchantInfo's tooltip callback, which highlights maxed enchantment tooltip lines
 * in bold.
 */
public class ModTooltipEvents {
    public static void onItemTooltip(ItemStack itemStack, List<Component> lines) {
        if (!getConfig().enchantInfoConfig.highlightMaxLevelEnchants) return;

        // enchanted books keep their enchantments in a separate component
        ItemEnchantments enchantments =
                itemStack.getOrDefault(
                        DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) enchantments = itemStack.getEnchantments();
        if (enchantments.isEmpty()) return;

        Set<String> maxedEnchantmentLines = new HashSet<>();
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            int level = entry.getIntValue();

            if (level == enchantment.value().getMaxLevel()) {
                maxedEnchantmentLines.add(Enchantment.getFullname(enchantment, level).getString());
            }
        }
        if (maxedEnchantmentLines.isEmpty()) return;

        // vanilla renders each enchantment as a line of exactly its full name, so exact matching
        // leaves lore and other mods' lines that merely start with an enchantment name untouched;
        // the first line is the item's name, which may itself read like an enchantment
        for (int i = 1; i < lines.size(); i++) {
            Component line = lines.get(i);

            if (maxedEnchantmentLines.contains(line.getString())) {
                lines.set(i, line.copy().withStyle(ChatFormatting.BOLD));
            }
        }
    }
}
