package io.github.brainage04.brainagehud.hud.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Lifecycle;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Exercises the enchantment information HUD's logic against a registry the test controls, so that
 * applicability, conflicting enchantments, maximum levels and the config blacklist are all asserted
 * exactly.
 *
 * <p>Vanilla enchantment data is loaded from datapack tags, which a headless test cannot bind
 * without a full resource reload; this test therefore builds its own enchantment registry. The
 * behaviours under test (supported items, exclusivity, maximum levels, blacklist filtering) are the
 * same mechanisms vanilla data uses.
 */
@SuppressWarnings("deprecation")
class EnchantInfoHudTest {
    private static final String NAMESPACE = "brainagehud_test";
    private static final TagKey<Enchantment> DAMAGE_EXCLUSIVE = enchantmentTag("damage_exclusive");
    private static final TagKey<Enchantment> MACE_EXCLUSIVE = enchantmentTag("mace_exclusive");

    private static MappedRegistry<Enchantment> enchantments;

    @BeforeAll
    static void bootstrapRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bindItemComponents();

        enchantments = new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());

        HolderSet<Enchantment> damageExclusive =
                HolderSet.emptyNamed(enchantments, DAMAGE_EXCLUSIVE);
        HolderSet<Enchantment> maceExclusive = HolderSet.emptyNamed(enchantments, MACE_EXCLUSIVE);
        HolderSet<Enchantment> noExclusivity = HolderSet.empty();

        Holder.Reference<Enchantment> sharpness =
                register(
                        "sharpness",
                        "Sharpness",
                        5,
                        damageExclusive,
                        Items.DIAMOND_SWORD,
                        Items.DIAMOND_AXE);
        Holder.Reference<Enchantment> smite =
                register("smite", "Smite", 5, damageExclusive, Items.DIAMOND_SWORD);
        Holder.Reference<Enchantment> baneOfArthropods =
                register(
                        "bane_of_arthropods",
                        "Bane of Arthropods",
                        5,
                        damageExclusive,
                        Items.DIAMOND_SWORD);
        Holder.Reference<Enchantment> breach =
                register("breach", "Breach", 4, maceExclusive, Items.MACE);
        Holder.Reference<Enchantment> density =
                register("density", "Density", 5, maceExclusive, Items.MACE);
        register("wind_burst", "Wind Burst", 3, noExclusivity, Items.MACE);
        register(
                "unbreaking",
                "Unbreaking",
                3,
                noExclusivity,
                Items.DIAMOND_SWORD,
                Items.DIAMOND_PICKAXE);
        register(
                "mending",
                "Mending",
                1,
                noExclusivity,
                Items.DIAMOND_SWORD,
                Items.DIAMOND_PICKAXE,
                Items.MACE);
        register("fire_aspect", "Fire Aspect", 2, noExclusivity, Items.DIAMOND_SWORD);
        register("looting", "Looting", 3, noExclusivity, Items.DIAMOND_SWORD);

        enchantments.bindTags(
                Map.of(
                        DAMAGE_EXCLUSIVE, List.of(sharpness, smite, baneOfArthropods),
                        MACE_EXCLUSIVE, List.of(breach, density)));
        enchantments.freeze();
    }

    @Test
    void diamondSwordListsPresentLevelsThenMissingEnchantments() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(enchantment("sharpness"), 3);
        sword.enchant(enchantment("unbreaking"), 3);

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments,
                        sword,
                        new EnchantInfoHudConfig(),
                        List.of(identifier("fire_aspect").toString()));

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

        TextList lines =
                EnchantInfoHud.getLines(enchantments, mace, new EnchantInfoHudConfig(), List.of());

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

        TextList lines =
                EnchantInfoHud.getLines(
                        enchantments,
                        sword,
                        new EnchantInfoHudConfig(),
                        List.of(identifier("fire_aspect").toString()));

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

    private static void bindItemComponents() {
        // vanilla binds item components while loading a world, so a headless test applies them
        // itself in order for
        // ItemStack construction - and therefore isEnchantable/isEnchanted - to behave as it does
        // in game
        HolderLookup.Provider vanillaRegistries = VanillaRegistries.createLookup();

        for (DataComponentInitializers.PendingComponents<?> pendingComponents :
                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(vanillaRegistries)) {
            pendingComponents.apply();
        }
    }

    private static Holder.Reference<Enchantment> register(
            String path,
            String name,
            int maxLevel,
            HolderSet<Enchantment> exclusiveSet,
            Item... supportedItems) {
        Enchantment enchantment =
                new Enchantment(
                        Component.literal(name),
                        Enchantment.definition(
                                HolderSet.direct(
                                        Arrays.stream(supportedItems)
                                                .map(BuiltInRegistries.ITEM::wrapAsHolder)
                                                .toList()),
                                10,
                                maxLevel,
                                Enchantment.constantCost(1),
                                Enchantment.constantCost(21),
                                1,
                                EquipmentSlotGroup.ANY),
                        exclusiveSet,
                        DataComponentMap.EMPTY);

        return enchantments.register(
                ResourceKey.create(Registries.ENCHANTMENT, identifier(path)),
                enchantment,
                RegistrationInfo.BUILT_IN);
    }

    private static Holder.Reference<Enchantment> enchantment(String path) {
        return enchantments.getOrThrow(
                ResourceKey.create(Registries.ENCHANTMENT, identifier(path)));
    }

    private static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static TagKey<Enchantment> enchantmentTag(String path) {
        return TagKey.create(Registries.ENCHANTMENT, identifier(path));
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
