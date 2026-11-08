package io.github.brainage04.brainagehud.hud.custom;

import static io.github.brainage04.brainagehud.TestEnchantments.enchantment;
import static io.github.brainage04.brainagehud.TestEnchantments.identifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.brainage04.brainagehud.TestEnchantments;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.List;
import net.minecraft.core.MappedRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises the enchantment information HUD's logic against {@link TestEnchantments}, so that
 * applicability, conflicting enchantments, maximum levels and the config blacklist are all asserted
 * exactly.
 */
class EnchantInfoHudTest {
    private static MappedRegistry<Enchantment> enchantments;

    @BeforeAll
    static void bootstrapRegistries() {
        enchantments = TestEnchantments.registry();
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

        // smite and bane of arthropods are excluded for conflicting with the sharpness already on
        // the sword, and
        // fire aspect is excluded by the blacklist
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

        // every enchantment available to a diamond sword is either present or blacklisted, so no
        // missing section
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
    void compactModeKeepsEachConflictGroupTogetherOnTheMissingLine() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;

        TextList lines =
                EnchantInfoHud.getLines(enchantments, new ItemStack(Items.MACE), config, List.of());

        printLines("compact: mace: no enchantments", lines);

        // groups are separated by commas and the enchantments within a group by slashes, as in
        // the full format
        assertEquals(
                List.of("Mace", "Missing: Breach / Density, Wind Burst, Mending"), rendered(lines));
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
                List.of("Breach / Density, Wind Burst, Mending"),
                rendered(
                        EnchantInfoHud.getLines(
                                enchantments, new ItemStack(Items.MACE), config, List.of())));
    }

    @Test
    void compactModeWrapsALongMissingLineBetweenConflictGroups() {
        EnchantInfoHudConfig config = new EnchantInfoHudConfig();
        config.compactMode = true;
        config.showItemName = false;

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments, new ItemStack(Items.DIAMOND_SWORD), config, List.of());

        printLines("compact: plain diamond sword, no enchantments", lines);

        // five missing groups would make one line of 90 characters; it wraps at 70 between groups
        assertEquals(
                List.of(
                        "Missing: Sharpness / Smite / Bane of Arthropods, Unbreaking, Mending",
                        "Fire Aspect, Looting"),
                rendered(lines));
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
