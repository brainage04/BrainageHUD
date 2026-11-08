package io.github.brainage04.brainagehud.hud.custom;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Displays the enchantments on the held item, followed by the enchantments that could still be
 * added to it. Enchantments that cannot coexist are collapsed onto a single "choose one" line, and
 * blacklisted enchantments are never shown as missing. Compact mode puts the enchantments on one
 * line and the missing enchantments on another, without maximum levels, so that a panel that was
 * one line per enchantment becomes at most three lines tall.
 */
public class EnchantInfoHud implements BasicCoreHudElement<EnchantInfoHudConfig> {
    /**
     * The longest compact line in characters. Compact lines are wrapped between their parts beyond
     * this, so that a long missing list cannot run off the screen at the usual GUI scales.
     */
    static final int COMPACT_LINE_LENGTH = 70;

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
                | (config.showMissingHeader ? 16 : 0)
                | (config.compactMode ? 32 : 0);
    }

    public static TextList getLines(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            ItemStack itemStack,
            EnchantInfoHudConfig config,
            Collection<String> blacklistedEnchantmentIds) {
        TextList lines = new TextList();

        if (!itemStack.isEnchantable() && !itemStack.isEnchanted()) return lines;

        ItemEnchantments enchantments = itemStack.getEnchantments();

        if (config.showItemName) lines.addHeader(itemStack.getHoverName());

        if (config.showEnchantments) {
            List<Component> presentEnchantments = new ArrayList<>();

            // registry order keeps the line order stable instead of following the enchantment
            // component's hash order
            enchantmentRegistry
                    .listElements()
                    .forEach(
                            enchantmentHolder -> {
                                int level = enchantments.getLevel(enchantmentHolder);

                                if (level <= 0) return;

                                presentEnchantments.add(
                                        formatPresentEnchantment(enchantmentHolder, level, config));
                            });

            if (config.compactMode) {
                if (!presentEnchantments.isEmpty()) {
                    addCompactLine(lines, Component.empty(), presentEnchantments);
                }
            } else {
                lines.addAll(presentEnchantments);
            }
        }

        if (config.showMissingEnchantments) {
            List<Holder.Reference<Enchantment>> missingEnchantments =
                    EnchantmentUtils.getMissingEnchantments(
                            enchantmentRegistry,
                            itemStack,
                            enchantments,
                            blacklistedEnchantmentIds);

            List<List<Holder.Reference<Enchantment>>> conflictGroups =
                    EnchantmentUtils.groupConflictingEnchantments(missingEnchantments);

            if (missingEnchantments.isEmpty()) return lines;

            if (config.compactMode) {
                List<Component> groupLines = new ArrayList<>();

                for (List<Holder.Reference<Enchantment>> conflictGroup : conflictGroups) {
                    List<Component> names = new ArrayList<>();

                    for (Holder.Reference<Enchantment> enchantmentHolder : conflictGroup) {
                        names.add(formatCompactEnchantmentName(enchantmentHolder));
                    }

                    groupLines.add(join(" / ", names));
                }

                Component prefix =
                        config.showMissingHeader
                                ? Component.literal("Missing: ").withStyle(ChatFormatting.BOLD)
                                : Component.empty();

                addCompactLine(lines, prefix, groupLines);
                return lines;
            }

            if (config.showMissingHeader) {
                lines.addHeader(Component.literal("Missing:"));
            }

            for (List<Holder.Reference<Enchantment>> conflictGroup : conflictGroups) {
                lines.add(formatMissingEnchantments(conflictGroup));
            }
        }

        return lines;
    }

    private static Component formatPresentEnchantment(
            Holder<Enchantment> enchantmentHolder, int level, EnchantInfoHudConfig config) {
        MutableComponent text = Enchantment.getFullname(enchantmentHolder, level).copy();
        int maxLevel = enchantmentHolder.value().getMaxLevel();

        // an enchantment shown without a maximum level is already at that maximum; compact mode
        // never shows it, so that every enchantment fits on one line
        if (config.showMaxLevels && !config.compactMode && level < maxLevel) {
            text.append(" (max %d)".formatted(maxLevel));
        }

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

    /**
     * Adds the compact form of one section: its parts joined with {@code ", "} on one line, wrapped
     * between parts once the line reaches {@link #COMPACT_LINE_LENGTH} characters. A wrapped part
     * is never split, so conflict groups stay together.
     */
    private static void addCompactLine(TextList lines, Component prefix, List<Component> parts) {
        MutableComponent line = Component.empty().append(prefix);
        int lineLength = prefix.getString().length();
        boolean lineHasPart = false;

        for (Component part : parts) {
            int partLength = part.getString().length();

            if (lineHasPart && lineLength + 2 + partLength > COMPACT_LINE_LENGTH) {
                lines.add(line);
                line = Component.empty();
                lineLength = 0;
                lineHasPart = false;
            }

            if (lineHasPart) {
                line.append(", ");
                lineLength += 2;
            }

            line.append(part);
            lineLength += partLength;
            lineHasPart = true;
        }

        lines.add(line);
    }

    /** Joins the parts onto one line, separated by {@code separator}. */
    private static MutableComponent join(String separator, List<Component> parts) {
        MutableComponent text = Component.empty();

        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) text.append(separator);

            text.append(parts.get(i));
        }

        return text;
    }

    /**
     * An enchantment's name in the colour {@link Enchantment#getFullname} gives it, without the
     * level that would make the compact missing line long.
     */
    private static Component formatCompactEnchantmentName(Holder<Enchantment> enchantmentHolder) {
        ChatFormatting colour =
                enchantmentHolder.is(EnchantmentTags.CURSE)
                        ? ChatFormatting.RED
                        : ChatFormatting.GRAY;

        return enchantmentHolder.value().description().copy().withStyle(colour);
    }

    @Override
    public EnchantInfoHudConfig getElementConfig() {
        return getConfig().enchantInfoHudConfig;
    }
}
