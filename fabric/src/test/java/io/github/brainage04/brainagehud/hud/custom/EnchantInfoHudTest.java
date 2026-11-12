package io.github.brainage04.brainagehud.hud.custom;

import static io.github.brainage04.brainagehud.TestEnchantments.enchantment;
import static io.github.brainage04.brainagehud.TestEnchantments.identifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.brainage04.brainagehud.SkyportEnchantments;
import io.github.brainage04.brainagehud.TestEnchantments;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.config.other.EnchantInfoConfig;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EnchantInfoHudTest {
    // max widths in characters, so that the expected lines can be counted by hand; in game they
    // are GUI pixels measured with the font (BrainageHUDClientGameTest checks those)
    private static final ToIntFunction<FormattedText> CHARACTERS =
            text -> text.getString().length();

    private static MappedRegistry<Enchantment> enchantments;
    private static MappedRegistry<Enchantment> skyport;

    @BeforeAll
    static void bootstrapRegistries() {
        enchantments = TestEnchantments.registry();
        skyport = SkyportEnchantments.registry();
    }

    @Test
    void diamondSwordListsPresentLevelsThenMissingEnchantments() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 3);
        sword.enchant(enchantment("unbreaking"), 3);

        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments, sword, config, List.of(identifier("fire_aspect").toString()));

        printLines("diamond sword: sharpness III + unbreaking III, fire aspect blacklisted", lines);

        assertEquals(
                List.of(
                        "Diamond Sword",
                        "Sharpness III (max 5)",
                        "Unbreaking III",
                        "Missing:",
                        "Mending",
                        "Looting III"),
                rendered(lines));
    }

    @Test
    void maceGroupsMutuallyExclusiveEnchantmentsOntoOneLine() {
        ItemStack mace = new ItemStack(Items.MACE);

        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;

        TextList lines = EnchantInfoHud.getLines(enchantments, mace, config, List.of());

        printLines("mace: no enchantments", lines);

        assertEquals(
                List.of("Mace", "Missing:", "Breach IV / Density V", "Wind Burst III", "Mending"),
                rendered(lines));
    }

    @Test
    void maxedItemReportsNoMissingEnchantments() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 5);
        sword.enchant(enchantment("unbreaking"), 3);
        sword.enchant(enchantment("mending"), 1);
        sword.enchant(enchantment("looting"), 3);

        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments, sword, config, List.of(identifier("fire_aspect").toString()));

        printLines(
                "diamond sword: sharpness V + unbreaking III + mending + looting III, fire aspect"
                        + " blacklisted",
                lines);

        assertEquals(
                List.of("Diamond Sword", "Sharpness V", "Unbreaking III", "Mending", "Looting III"),
                rendered(lines));
    }

    @Test
    void rendersNothingWhenNothingIsHeldOrTheItemIsNotApplicable() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();

        assertTrue(
                EnchantInfoHud.getLines(enchantments, ItemStack.EMPTY, config, List.of())
                        .isEmpty());
        assertTrue(
                EnchantInfoHud.getLines(enchantments, new ItemStack(Items.STONE), config, List.of())
                        .isEmpty());
    }

    @Test
    void disabledSectionsDoNotLeaveHeadersOrLevelSuffixes() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 3);
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;
        config.showItemName = false;
        config.showMaxLevels = false;
        config.showMissingEnchantments = false;

        assertEquals(
                List.of("Sharpness III"),
                rendered(EnchantInfoHud.getLines(enchantments, sword, config, List.of())));

        config.showEnchantments = false;
        assertTrue(EnchantInfoHud.getLines(enchantments, sword, config, List.of()).isEmpty());
    }

    @Test
    void missingCandidatesRemainVisibleWithoutTheirHeader() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;
        config.showItemName = false;
        config.showEnchantments = false;
        config.showMissingHeader = false;

        assertEquals(
                List.of("Breach IV / Density V", "Wind Burst III", "Mending"),
                rendered(
                        EnchantInfoHud.getLines(
                                enchantments, new ItemStack(Items.MACE), config, List.of())));
    }

    @Test
    void compactModeUsesOneLinePerSectionWithoutMaximumLevels() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 3);
        sword.enchant(enchantment("unbreaking"), 3);

        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments, sword, config, List.of(identifier("fire_aspect").toString()));

        printLines(
                "compact: diamond sword: sharpness III + unbreaking III, fire aspect blacklisted",
                lines);

        // the maximum levels are dropped and the missing enchantments lose their levels too, so
        // the panel is three lines instead of six
        assertEquals(
                List.of(
                        "Diamond Sword",
                        "Sharpness III, Unbreaking III",
                        "Missing: Mending, Looting"),
                rendered(lines));
    }

    @Test
    void compactModePutsEachConflictGroupOnALineOfItsOwn() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;

        TextList lines =
                EnchantInfoHud.getLines(enchantments, new ItemStack(Items.MACE), config, List.of());

        printLines("compact: mace: no enchantments", lines);

        // commas only join enchantments that can all be added; the slashes of a conflict group
        // start a line of their own, as in the full format
        assertEquals(
                List.of("Mace", "Missing: Wind Burst, Mending", "Breach / Density"),
                rendered(lines));
    }

    @Test
    void compactModeOffKeepsTheFullFormat() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 2);

        EnchantInfoHudConfig compact = new EnchantInfoHudConfig();
        compact.compactMode = true;
        EnchantInfoHudConfig full = new EnchantInfoHudConfig();
        full.compactMode = false;

        assertEquals(
                List.of(
                        "Diamond Sword",
                        "Sharpness II",
                        "Missing: Unbreaking, Mending, Fire Aspect, Looting"),
                rendered(EnchantInfoHud.getLines(enchantments, sword, compact, List.of())));

        assertEquals(
                List.of(
                        "Diamond Sword",
                        "Sharpness II (max 5)",
                        "Missing:",
                        "Unbreaking III",
                        "Mending",
                        "Fire Aspect II",
                        "Looting III"),
                rendered(EnchantInfoHud.getLines(enchantments, sword, full, List.of())));
    }

    @Test
    void compactModeWithoutTheMissingHeaderStillShowsTheMissingEnchantments() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;
        config.showItemName = false;
        config.showEnchantments = false;
        config.showMissingHeader = false;

        assertEquals(
                List.of("Wind Burst, Mending", "Breach / Density"),
                rendered(
                        EnchantInfoHud.getLines(
                                enchantments, new ItemStack(Items.MACE), config, List.of())));
    }

    @Test
    void compactModeWithoutAMaxWidthKeepsEachSectionOnOneLine() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;

        TextList lines =
                EnchantInfoHud.getLines(
                        skyport,
                        new ItemStack(Items.IRON_SWORD),
                        config,
                        new EnchantInfoConfig().blacklistedEnchantmentIds);

        printLines("compact: Skyport iron sword, no max width", lines);

        // the owner's case of 9 and 12 Nov 2026; the default blacklist hides Smite, Bane of
        // Arthropods and Knockback
        assertEquals(
                List.of(
                        "Iron Sword",
                        "Missing: Blinding, Bloodlust, Crippling, Dread, Drifting, Eclipse,"
                                + " Enfeebling, Famine, Fire Aspect, Infestation, Insight, Looting,"
                                + " Mending, Oozing, Plague, Pyre, Sapping, Siphon, Soulbound,"
                                + " Stormcaller, Sweeping Edge, Unbreaking, Updraft, Venom, Vertigo,"
                                + " Warden's Wrath, Warding, Withering",
                        "Annihilation / Bane of Illagers / Bane of the Deep / Bane of the End /"
                                + " Bane of the Nether / Duelist / Sharpness",
                        "Cataclysm / Volatile"),
                rendered(lines));
    }

    @Test
    void compactModeWrapsAtTheMaxWidthOnlyBetweenNames() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;

        TextList lines =
                EnchantInfoHud.getLines(
                        skyport,
                        new ItemStack(Items.IRON_SWORD),
                        config,
                        new EnchantInfoConfig().blacklistedEnchantmentIds,
                        48,
                        CHARACTERS);

        printLines("compact: Skyport iron sword, max width 48 characters", lines);

        assertEquals(
                List.of(
                        "Iron Sword",
                        "Missing: Blinding, Bloodlust, Crippling, Dread,",
                        "Drifting, Eclipse, Enfeebling, Famine,",
                        "Fire Aspect, Infestation, Insight, Looting,",
                        "Mending, Oozing, Plague, Pyre, Sapping, Siphon,",
                        "Soulbound, Stormcaller, Sweeping Edge,",
                        "Unbreaking, Updraft, Venom, Vertigo,",
                        "Warden's Wrath, Warding, Withering",
                        "Annihilation / Bane of Illagers /",
                        "Bane of the Deep / Bane of the End /",
                        "Bane of the Nether / Duelist / Sharpness",
                        "Cataclysm / Volatile"),
                rendered(lines));
        for (String line : rendered(lines)) assertTrue(line.length() <= 48, line);
        assertEveryNameWhole(lines);
    }

    @Test
    void aNameWiderThanTheMaxWidthStaysWholeOnItsOwnLine() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;
        config.showItemName = false;

        TextList lines =
                EnchantInfoHud.getLines(
                        skyport,
                        new ItemStack(Items.IRON_SWORD),
                        config,
                        new EnchantInfoConfig().blacklistedEnchantmentIds,
                        12,
                        CHARACTERS);

        printLines("compact: Skyport iron sword, max width 12 characters", lines);

        List<String> rendered = rendered(lines);
        assertEquals("Missing: Blinding,", rendered.getFirst());
        assertTrue(rendered.contains("Warden's Wrath,"), rendered.toString());
        assertTrue(rendered.contains("Bane of the Nether /"), rendered.toString());
        assertEquals(
                List.of("Cataclysm /", "Volatile"),
                rendered.subList(rendered.size() - 2, rendered.size()));
        assertEveryNameWhole(lines);
    }

    @Test
    void aLongItemNameWrapsBetweenWordsAndStaysBold() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.set(
                DataComponents.CUSTOM_NAME,
                Component.literal("The Unbreakable Blade of the Northern Wastes Forged Anew"));
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;
        config.showMissingEnchantments = false;

        TextList lines =
                EnchantInfoHud.getLines(
                        skyport,
                        sword,
                        config,
                        new EnchantInfoConfig().blacklistedEnchantmentIds,
                        20,
                        CHARACTERS);

        assertEquals(
                List.of("The Unbreakable", "Blade of the", "Northern Wastes", "Forged Anew"),
                rendered(lines));
        for (Component line : lines) {
            line.visit(
                    (style, text) -> {
                        if (!text.isBlank()) assertTrue(style.isBold(), text);
                        return Optional.empty();
                    },
                    Style.EMPTY);
        }
    }

    @Test
    void theCachedLinesFollowAFontChangeWithTheSameItem() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        List<String> blacklist = new EnchantInfoConfig().blacklistedEnchantmentIds;
        // a wider font, as Force Unicode Font or a resource pack can switch to while the item stays
        ToIntFunction<FormattedText> wider = text -> 2 * text.getString().length();
        EnchantInfoHud hud = new EnchantInfoHud();

        hud.cachedLines(skyport, sword, config, blacklist, 48, CHARACTERS);
        TextList afterFontChange = hud.cachedLines(skyport, sword, config, blacklist, 48, wider);

        assertEquals(
                rendered(EnchantInfoHud.getLines(skyport, sword, config, blacklist, 48, wider)),
                rendered(afterFontChange));
        for (Component line : afterFontChange) {
            String text = line.getString();
            // a line holding a single name may be wider; every other line fits
            if (text.contains(", ") || text.contains(" / ")) {
                assertTrue(wider.applyAsInt(line) <= 48, text);
            }
        }
    }

    @Test
    void theFullFormatWrapsConflictGroupsAtTheMaxWidthAndKeepsOtherLinesWhole() {
        ItemStack sword = new ItemStack(Items.IRON_SWORD);
        sword.enchant(
                skyport.getOrThrow(
                        ResourceKey.create(
                                Registries.ENCHANTMENT, Identifier.parse("skyport:wardens_wrath"))),
                2);
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = false;

        List<String> rendered =
                rendered(
                        EnchantInfoHud.getLines(
                                skyport,
                                sword,
                                config,
                                new EnchantInfoConfig().blacklistedEnchantmentIds,
                                30,
                                CHARACTERS));

        // the present enchantment and the single missing ones are one name each and stay whole
        assertEquals("Warden's Wrath II (max 3)", rendered.get(1));
        assertTrue(rendered.contains("Sweeping Edge III"), rendered.toString());
        int group = rendered.indexOf("Annihilation III /");
        assertEquals(
                List.of(
                        "Annihilation III /",
                        "Bane of Illagers III /",
                        "Bane of the Deep III /",
                        "Bane of the End III /",
                        "Bane of the Nether III /",
                        "Duelist III / Sharpness III"),
                rendered.subList(group, group + 6));
        assertTrue(rendered.contains("Cataclysm III / Volatile III"), rendered.toString());
    }

    @Test
    void noMaxWidthGivesTheSameLinesAsBefore() {
        for (boolean compact : List.of(true, false)) {
            EnchantInfoHudConfig config = new EnchantInfoHudConfig();
            config.compactMode = compact;
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            List<String> blacklist = new EnchantInfoConfig().blacklistedEnchantmentIds;

            assertEquals(
                    rendered(EnchantInfoHud.getLines(skyport, sword, config, blacklist)),
                    rendered(
                            EnchantInfoHud.getLines(
                                    skyport, sword, config, blacklist, 0, text -> 1000)));
        }
    }

    /** Every Skyport and vanilla name the lines mention is whole on one line, never split. */
    private static void assertEveryNameWhole(TextList lines) {
        String text = String.join("\n", rendered(lines));
        skyport.listElements()
                .forEach(
                        holder -> {
                            String name = holder.value().description().getString();
                            String id = holder.key().identifier().toString();
                            boolean blacklisted =
                                    new EnchantInfoConfig().blacklistedEnchantmentIds.contains(id);
                            if (!blacklisted) assertTrue(text.contains(name), name);
                        });
    }

    private static List<String> rendered(TextList lines) {
        return lines.stream().map(Component::getString).toList();
    }

    private static void printLines(String label, TextList lines) {
        System.out.println(label);
        for (Component line : lines) {
            System.out.println("  " + line.getString());
        }
    }
}
