package io.github.brainage04.brainagehud.command;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Ported from GetEnchantInfo's {@code io.github.brainage04.commands.BlacklistedEnchantsCommand}.
 *
 * <p>The blacklist is stored as enchantment IDs in the config, which is the only source of truth.
 */
public class BlacklistedEnchantsCommand {
    public static int executeAdd(Holder<Enchantment> enchantmentHolder) {
        List<String> blacklist = getBlacklist();
        String enchantmentId = EnchantmentUtils.getEnchantmentId(enchantmentHolder);

        if (blacklist.contains(enchantmentId)) {
            feedback(
                    EnchantmentUtils.getEnchantmentName(enchantmentHolder)
                            .append(" is already blacklisted!"));

            return 0;
        }

        blacklist.add(enchantmentId);
        ConfigUtils.saveConfig();

        feedback(
                Component.empty()
                        .append(EnchantmentUtils.getEnchantmentName(enchantmentHolder))
                        .append(" is now blacklisted."));

        return 1;
    }

    public static int executeRemove(Holder<Enchantment> enchantmentHolder) {
        if (!getBlacklist().remove(EnchantmentUtils.getEnchantmentId(enchantmentHolder))) {
            feedback(
                    EnchantmentUtils.getEnchantmentName(enchantmentHolder)
                            .append(" is not blacklisted!"));

            return 0;
        }

        ConfigUtils.saveConfig();

        feedback(
                Component.empty()
                        .append(EnchantmentUtils.getEnchantmentName(enchantmentHolder))
                        .append(" is no longer blacklisted."));

        return 1;
    }

    public static int executeQuery() {
        List<String> blacklist = getBlacklist();
        if (blacklist.isEmpty()) {
            feedback(Component.literal("No blacklisted enchantments."));

            return 1;
        }

        var enchantmentRegistry =
                Minecraft.getInstance()
                        .level
                        .registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT);

        feedback(Component.literal("Enchantment blacklist:"));

        for (String enchantmentId : blacklist) {
            Identifier identifier = Identifier.tryParse(enchantmentId);
            // IDs of enchantments this world does not have are listed verbatim
            Component name =
                    identifier == null
                            ? Component.literal(enchantmentId)
                            : enchantmentRegistry
                                    .get(ResourceKey.create(Registries.ENCHANTMENT, identifier))
                                    .<Component>map(EnchantmentUtils::getEnchantmentName)
                                    .orElseGet(() -> Component.literal(enchantmentId));

            feedback(Component.literal(" - ").append(name));
        }

        return 1;
    }

    private static List<String> getBlacklist() {
        return ConfigUtils.getConfig().enchantInfoConfig.blacklistedEnchantmentIds;
    }
}
