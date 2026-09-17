package io.github.brainage04.brainagehud.command;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Ported from GetEnchantInfo's {@code io.github.brainage04.commands.BlacklistedEnchantsCommand}.
 */
public class BlacklistedEnchantsCommand {
    private static final Set<Enchantment> blacklistedEnchants = new LinkedHashSet<>();

    public static Set<Enchantment> getBlacklistedEnchants() {
        return blacklistedEnchants;
    }

    public static int executeAdd(
            SharedSuggestionProvider source, Holder<Enchantment> enchantmentHolder) {
        synchronizeBlacklist();
        Enchantment enchantment = enchantmentHolder.value();

        if (blacklistedEnchants.contains(enchantment)) {
            feedback(
                    EnchantmentUtils.getEnchantmentName(enchantmentHolder)
                            .append(" is already blacklisted!"));

            return 0;
        }

        blacklistedEnchants.add(enchantment);
        ConfigUtils.getConfig()
                .enchantInfoConfig
                .blacklistedEnchantmentIds
                .add(EnchantmentUtils.getEnchantmentId(enchantmentHolder));

        feedback(
                Component.empty()
                        .append(EnchantmentUtils.getEnchantmentName(enchantmentHolder))
                        .append(" is now blacklisted."));

        ConfigUtils.saveConfig();

        return 1;
    }

    public static int executeRemove(
            SharedSuggestionProvider source, Holder<Enchantment> enchantmentHolder) {
        synchronizeBlacklist();
        Enchantment enchantment = enchantmentHolder.value();

        if (!blacklistedEnchants.contains(enchantment)) {
            feedback(
                    EnchantmentUtils.getEnchantmentName(enchantmentHolder)
                            .append(" is not blacklisted!"));

            return 0;
        }

        blacklistedEnchants.remove(enchantment);
        ConfigUtils.getConfig()
                .enchantInfoConfig
                .blacklistedEnchantmentIds
                .remove(EnchantmentUtils.getEnchantmentId(enchantmentHolder));

        feedback(
                Component.empty()
                        .append(EnchantmentUtils.getEnchantmentName(enchantmentHolder))
                        .append(" is no longer blacklisted."));

        ConfigUtils.saveConfig();

        return 1;
    }

    public static int executeQuery(SharedSuggestionProvider source) {
        synchronizeBlacklist();
        if (blacklistedEnchants.isEmpty()) {
            feedback(Component.literal("No blacklisted enchantments."));

            return 1;
        }

        Registry<Enchantment> enchantmentRegistry =
                Minecraft.getInstance()
                        .level
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT);

        feedback(Component.literal("Enchantment blacklist:"));

        for (Enchantment enchantment : blacklistedEnchants) {
            feedback(
                    Component.literal(" - ")
                            .append(
                                    EnchantmentUtils.getEnchantmentName(
                                            enchantmentRegistry.wrapAsHolder(enchantment))));
        }

        return 1;
    }

    public static void synchronizeBlacklist(Registry<Enchantment> enchantmentRegistry) {
        blacklistedEnchants.clear();
        for (String enchantmentId :
                ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds) {
            enchantmentRegistry
                    .getOptional(Identifier.parse(enchantmentId))
                    .ifPresent(blacklistedEnchants::add);
        }
    }

    private static void synchronizeBlacklist() {
        synchronizeBlacklist(
                Minecraft.getInstance()
                        .level
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT));
    }

    private static void feedback(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.sendSystemMessage(message);
    }
}
