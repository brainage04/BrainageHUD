package io.github.brainage04.brainagehud.hud.custom;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.util.EnchantmentUtils;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import io.github.brainage04.hudrendererlib.util.TextWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Displays the enchantments on the held item, followed by the enchantments that could still be
 * added to it. Enchantments that cannot coexist are collapsed onto a single "choose one" line, and
 * blacklisted enchantments are never shown as missing. Compact mode puts the enchantments on one
 * line and the missing enchantments that can all be added on another, without maximum levels,
 * followed by one line per conflict group.
 *
 * <p>Separators: {@code ", "} only ever joins enchantments that can all be added, and {@code " / "}
 * only ever joins the alternatives of one conflict group, which starts a line of its own. Lines
 * wrap only at these separators, never inside an enchantment's name, and a wrapped line keeps the
 * separator it broke at ({@code ","} or {@code " /"}) at its end. With the element's Max Width set,
 * every line wraps at it; without, compact lines wrap at {@link #COMPACT_LINE_LENGTH} characters
 * and the full format does not wrap.
 */
public class EnchantInfoHud implements BasicCoreHudElement<EnchantInfoHudConfig> {
    /**
     * The longest compact line in characters while the element has no max width, so that a long
     * missing list cannot run off the screen at the usual GUI scales.
     */
    static final int COMPACT_LINE_LENGTH = 70;

    private static final ToIntFunction<FormattedText> CHARACTERS =
            text -> text.getString().length();

    // the lines only change with their inputs, so they are rebuilt only when an input changes
    // rather than walking the enchantment registry every frame
    private TextList cachedLines;
    private HolderLookup.RegistryLookup<Enchantment> cachedRegistry;
    private ItemStack cachedStack = ItemStack.EMPTY;
    private int cachedConfigFlags;
    private int cachedMaxWidth;
    private List<String> cachedBlacklist = List.of();

    /** The lines are wrapped at the element's max width here, at the enchantment separators. */
    @Override
    public boolean wrapsOwnLines() {
        return true;
    }

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
        int maxWidth = HudRendererLib.getMaxWidth(config.coreSettings);
        List<String> blacklist = getConfig().enchantInfoConfig.blacklistedEnchantmentIds;

        if (cachedLines == null
                || registry != cachedRegistry
                || configFlags != cachedConfigFlags
                || maxWidth != cachedMaxWidth
                || !ItemStack.matches(stack, cachedStack)
                || !blacklist.equals(cachedBlacklist)) {
            cachedLines =
                    getLines(
                            registry,
                            stack,
                            config,
                            blacklist,
                            maxWidth,
                            TextWrapper.widthOf(minecraft.font));
            cachedRegistry = registry;
            cachedStack = stack.copy();
            cachedConfigFlags = configFlags;
            cachedMaxWidth = maxWidth;
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

    /** The lines without a max width, as {@link #getLines()} gives them while it is off. */
    public static TextList getLines(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            ItemStack itemStack,
            EnchantInfoHudConfig config,
            Collection<String> blacklistedEnchantmentIds) {
        return getLines(
                enchantmentRegistry, itemStack, config, blacklistedEnchantmentIds, 0, CHARACTERS);
    }

    /**
     * The lines wrapped at {@code maxWidth} as measured by {@code width}; a {@code maxWidth} of 0
     * or less means the element has none.
     */
    public static TextList getLines(
            HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry,
            ItemStack itemStack,
            EnchantInfoHudConfig config,
            Collection<String> blacklistedEnchantmentIds,
            int maxWidth,
            ToIntFunction<FormattedText> width) {
        TextList lines = new TextList();
        // compact lines keep their own limit while the element has no max width
        int compactMaxWidth = maxWidth > 0 ? maxWidth : COMPACT_LINE_LENGTH;
        ToIntFunction<FormattedText> compactWidth = maxWidth > 0 ? width : CHARACTERS;

        // an item that is neither enchantable nor enchanted has nothing to report
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
                    lines.addAll(
                            TextWrapper.wrapParts(
                                    Component.empty(),
                                    presentEnchantments,
                                    ", ",
                                    compactMaxWidth,
                                    compactWidth));
                }
            } else {
                // one enchantment per line, and a name is never split
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
                Component prefix =
                        config.showMissingHeader
                                ? Component.literal("Missing: ").withStyle(ChatFormatting.BOLD)
                                : Component.empty();
                List<Component> compatible = new ArrayList<>();
                List<List<Component>> alternatives = new ArrayList<>();

                for (List<Holder.Reference<Enchantment>> conflictGroup : conflictGroups) {
                    List<Component> names = new ArrayList<>();

                    for (Holder.Reference<Enchantment> enchantmentHolder : conflictGroup) {
                        names.add(formatCompactEnchantmentName(enchantmentHolder));
                    }

                    if (names.size() == 1) compatible.addAll(names);
                    else alternatives.add(names);
                }

                // the enchantments that can all be added share the "Missing:" line(s), and each
                // conflict group follows on lines of its own, as in the full format
                if (!compatible.isEmpty()) {
                    lines.addAll(
                            TextWrapper.wrapParts(
                                    prefix, compatible, ", ", compactMaxWidth, compactWidth));
                    prefix = Component.empty();
                }

                for (List<Component> names : alternatives) {
                    lines.addAll(
                            TextWrapper.wrapParts(
                                    prefix, names, " / ", compactMaxWidth, compactWidth));
                    prefix = Component.empty();
                }

                return lines;
            }

            if (config.showMissingHeader) {
                lines.addHeader(Component.literal("Missing:"));
            }

            for (List<Holder.Reference<Enchantment>> conflictGroup : conflictGroups) {
                lines.addAll(
                        TextWrapper.wrapParts(
                                Component.empty(),
                                formatMissingEnchantments(conflictGroup),
                                " / ",
                                maxWidth,
                                width));
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

    /** The enchantments of a conflict group at their maximum levels. */
    private static List<Component> formatMissingEnchantments(
            List<Holder.Reference<Enchantment>> enchantments) {
        List<Component> names = new ArrayList<>();

        for (Holder<Enchantment> enchantmentHolder : enchantments) {
            names.add(
                    Enchantment.getFullname(
                            enchantmentHolder, enchantmentHolder.value().getMaxLevel()));
        }

        return names;
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
