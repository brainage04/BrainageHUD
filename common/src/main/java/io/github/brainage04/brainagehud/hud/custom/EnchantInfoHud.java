package io.github.brainage04.brainagehud.hud.custom;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Displays the enchantments on the held item, followed by the enchantments that could still be
 * added to it. Enchantments that cannot coexist are collapsed onto a single "choose one" line, and
 * blacklisted enchantments are never shown as missing.
 */
public class EnchantInfoHud implements BasicCoreHudElement<EnchantInfoHudConfig> {
    // the lines only change with their inputs, so they are rebuilt only when an input changes
    // rather than walking the enchantment registry every frame
    private TextList cachedLines;
    private HolderLookup.RegistryLookup<Enchantment> cachedRegistry;
    private ItemStack cachedStack = ItemStack.EMPTY;
    private int cachedConfigFlags;
    private List<String> cachedBlacklist = List.of();

    @Override
    public TextList getLines() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null || level == null) return new TextList();

        HolderLookup.RegistryLookup<Enchantment> registry =
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack stack = player.getMainHandItem();
        EnchantInfoHudConfig config = getElementConfig();
        int configFlags = configFlags(config);
        List<String> blacklist = getConfig().enchantInfoConfig.blacklistedEnchantmentIds;

        if (cachedLines == null
                || registry != cachedRegistry
                || configFlags != cachedConfigFlags
                || !ItemStack.matches(stack, cachedStack)
                || !blacklist.equals(cachedBlacklist)) {
            cachedLines = getLines(registry, stack, config, blacklist);
            cachedRegistry = registry;
            cachedStack = stack.copy();
            cachedConfigFlags = configFlags;
            cachedBlacklist = List.copyOf(blacklist);
        }

        return cachedLines;
    }

    private static int configFlags(EnchantInfoHudConfig config) {
        return (config.showItemName ? 1 : 0)
                | (config.showEnchantments ? 2 : 0)
                | (config.showMaxLevels ? 4 : 0)
                | (config.showMissingEnchantments ? 8 : 0)
                | (config.showMissingHeader ? 16 : 0);
    }

    public static TextList getLines(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            ItemStack itemStack,
            EnchantInfoHudConfig config,
            Collection<String> blacklistedEnchantmentIds) {
        TextList lines = new TextList();

        // an item that is neither enchantable nor enchanted has nothing to report
        if (!itemStack.isEnchantable() && !itemStack.isEnchanted()) return lines;

        ItemEnchantments enchantments = itemStack.getEnchantments();

        if (config.showItemName) lines.addHeader(itemStack.getHoverName());

        if (config.showEnchantments) {
            // registry order keeps the line order stable instead of following the enchantment
            // component's hash order
            enchantmentRegistry
                    .listElements()
                    .forEach(
                            enchantmentHolder -> {
                                int level = enchantments.getLevel(enchantmentHolder);

                                if (level <= 0) return;

                                lines.add(
                                        formatPresentEnchantment(enchantmentHolder, level, config));
                            });
        }

        if (config.showMissingEnchantments) {
            List<Holder.Reference<Enchantment>> missingEnchantments =
                    EnchantmentUtils.getMissingEnchantments(
                            enchantmentRegistry,
                            itemStack,
                            enchantments,
                            blacklistedEnchantmentIds);

            if (!missingEnchantments.isEmpty() && config.showMissingHeader) {
                lines.addHeader(Component.literal("Missing:"));
            }

            for (List<Holder.Reference<Enchantment>> conflictGroup :
                    EnchantmentUtils.groupConflictingEnchantments(missingEnchantments)) {
                lines.add(formatMissingEnchantments(conflictGroup));
            }
        }

        return lines;
    }

    private static Component formatPresentEnchantment(
            Holder<Enchantment> enchantmentHolder, int level, EnchantInfoHudConfig config) {
        MutableComponent text = Enchantment.getFullname(enchantmentHolder, level).copy();
        int maxLevel = enchantmentHolder.value().getMaxLevel();

        // an enchantment shown without a maximum level is already at that maximum
        if (config.showMaxLevels && level < maxLevel) text.append(" (max %d)".formatted(maxLevel));

        return text;
    }

    private static Component formatMissingEnchantments(
            List<Holder.Reference<Enchantment>> enchantments) {
        MutableComponent text = Component.empty();

        for (int i = 0; i < enchantments.size(); i++) {
            if (i > 0) text.append(" / ");

            Holder<Enchantment> enchantmentHolder = enchantments.get(i);

            text.append(
                    Enchantment.getFullname(
                            enchantmentHolder, enchantmentHolder.value().getMaxLevel()));
        }

        return text;
    }

    @Override
    public EnchantInfoHudConfig getElementConfig() {
        return getConfig().enchantInfoHudConfig;
    }
}
