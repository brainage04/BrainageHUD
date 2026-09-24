package io.github.brainage04.brainagehud.command;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Ported from GetEnchantInfo's {@code io.github.brainage04.commands.GetEnchantsCommand}. */
public class GetEnchantsCommand {
    public static int execute(ItemStack itemStack) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry =
                Minecraft.getInstance()
                        .level
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT);
        List<String> blacklist = ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds;

        List<Holder.Reference<Enchantment>> acceptableEnchantments = new ArrayList<>();
        enchantmentRegistry
                .listElements()
                .filter(enchantment -> !blacklist.contains(EnchantmentUtils.getEnchantmentId(enchantment)))
                .filter(enchantment -> enchantment.value().canEnchant(itemStack))
                .forEach(acceptableEnchantments::add);

        if (acceptableEnchantments.isEmpty()) {
            feedback(Component.literal("No acceptable enchantments found!"));

            return 1;
        }

        List<List<Holder.Reference<Enchantment>>> conflictGroups = new ArrayList<>();
        List<Holder.Reference<Enchantment>> unconflictedEnchantments = new ArrayList<>();
        for (List<Holder.Reference<Enchantment>> group :
                EnchantmentUtils.groupConflictingEnchantments(acceptableEnchantments)) {
            if (group.size() == 1) {
                unconflictedEnchantments.add(group.getFirst());
            } else {
                conflictGroups.add(group);
            }
        }

        feedback(
                Component.literal("Acceptable enchants for ")
                        .append(itemStack.getHoverName())
                        .append(":")
                        .withStyle(ChatFormatting.BOLD));

        if (!conflictGroups.isEmpty()) {
            // groups are linked by conflicts, not necessarily all mutually exclusive: riptide
            // excludes both loyalty and channeling, which can still be combined with each other
            feedback(Component.literal("Enchantments that conflict within each group:"));

            for (List<Holder.Reference<Enchantment>> group : conflictGroups) {
                feedback(
                        Component.literal(" - ")
                                .append(EnchantmentUtils.joinEnchantmentNames(group, itemStack)));
            }
        }

        if (!unconflictedEnchantments.isEmpty()) {
            feedback(Component.literal("Enchantments with no conflicts:"));

            for (Holder<Enchantment> enchantment : unconflictedEnchantments) {
                feedback(
                        Component.literal(" - ")
                                .append(EnchantmentUtils.getEnchantmentName(enchantment, itemStack)));
            }
        }

        return 1;
    }

    public static int execute(Holder<Item> item) {
        return execute(item.value().getDefaultInstance());
    }

    public static int execute() {
        return execute(Minecraft.getInstance().player.getInventory().getSelectedItem());
    }
}
