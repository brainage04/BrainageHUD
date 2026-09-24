package io.github.brainage04.brainagehud.command;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;

import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;

/** Ported from GetEnchantInfo's {@code io.github.brainage04.commands.GetEnchantInfoCommand}. */
public class GetEnchantInfoCommand {
    public static void sendEnchantmentInfo(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            Holder<Enchantment> enchantmentHolder) {
        Enchantment enchantment = enchantmentHolder.value();

        feedback(
                Component.literal("Enchant info for ")
                        .append(EnchantmentUtils.getEnchantmentName(enchantmentHolder))
                        .append(":")
                        .withStyle(ChatFormatting.BOLD));

        feedback(Component.literal("ID: %s".formatted(EnchantmentUtils.getEnchantmentId(enchantmentHolder))));
        feedback(Component.literal("Max level: %d".formatted(enchantment.getMaxLevel())));
        feedback(
                Component.literal("Incompatible with: ")
                        .append(joinIncompatibleEnchantmentNames(enchantmentRegistry, enchantmentHolder)));
        feedback(
                Component.literal("Applied to: ")
                        .append(
                                joinNames(
                                        enchantment.getSupportedItems().stream()
                                                .map(item -> item.value().getName(item.value().getDefaultInstance()))
                                                .toList())));
    }

    /**
     * The enchantments whose name matches {@code query}, ignoring case: the exact match alone if
     * there is one, otherwise every enchantment whose name contains the query.
     */
    public static List<Holder.Reference<Enchantment>> findMatches(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry, String query) {
        String normalisedQuery = query.toLowerCase(Locale.ROOT);
        List<Holder.Reference<Enchantment>> potentialMatches = new ArrayList<>();

        for (Holder.Reference<Enchantment> enchantment : enchantmentRegistry.listElements().toList()) {
            String name =
                    EnchantmentUtils.getEnchantmentName(enchantment)
                            .getString()
                            .toLowerCase(Locale.ROOT);

            if (name.equals(normalisedQuery)) return List.of(enchantment);

            if (name.contains(normalisedQuery)) potentialMatches.add(enchantment);
        }

        return potentialMatches;
    }

    public static int execute(String desiredEnchantmentString) {
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = getEnchantmentRegistry();
        List<Holder.Reference<Enchantment>> matches = findMatches(enchantmentRegistry, desiredEnchantmentString);

        if (matches.size() == 1) {
            Holder.Reference<Enchantment> match = matches.getFirst();
            feedback(
                    Component.literal("Exact match found - ")
                            .append(EnchantmentUtils.getEnchantmentName(match)));

            sendEnchantmentInfo(enchantmentRegistry, match);

            return 1;
        }

        if (matches.isEmpty()) {
            feedback(Component.literal("No potential matches found!"));

            return 0;
        }

        feedback(Component.literal("No exact match found. Potential matches:"));

        for (Holder.Reference<Enchantment> enchantment : matches) {
            feedback(
                    Component.empty()
                            .append(EnchantmentUtils.getEnchantmentName(enchantment))
                            .append(" - ")
                            .append(EnchantmentUtils.getEnchantmentId(enchantment)));
        }

        return 1;
    }

    public static int execute(Holder<Enchantment> enchantmentHolder) {
        sendEnchantmentInfo(getEnchantmentRegistry(), enchantmentHolder);

        return 1;
    }

    private static Component joinIncompatibleEnchantmentNames(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            Holder<Enchantment> baseEnchantment) {
        List<Component> conflicts =
                enchantmentRegistry
                        .listElements()
                        .filter(enchantment -> enchantment.value() != baseEnchantment.value())
                        .filter(enchantment -> !Enchantment.areCompatible(baseEnchantment, enchantment))
                        .<Component>map(EnchantmentUtils::getEnchantmentName)
                        .toList();

        return conflicts.isEmpty() ? Component.literal("N/A") : joinNames(conflicts);
    }

    private static Component joinNames(List<Component> names) {
        MutableComponent text = Component.empty();

        for (int i = 0; i < names.size(); i++) {
            if (i > 0) text.append(", ");

            text.append(names.get(i));
        }

        return text;
    }

    private static HolderLookup.RegistryLookup<Enchantment> getEnchantmentRegistry() {
        return Minecraft.getInstance()
                .level
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);
    }
}
