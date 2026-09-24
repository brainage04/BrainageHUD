package io.github.brainage04.brainagehud;

import com.mojang.serialization.Lifecycle;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * An enchantment registry the tests control, so that applicability, conflicts, maximum levels and
 * names are all known exactly.
 *
 * <p>Vanilla enchantment data is loaded from datapack tags, which a headless test cannot bind
 * without a full resource reload; this registry therefore defines its own enchantments. The
 * mechanisms under test (supported items, exclusivity, maximum levels) are the same ones vanilla
 * data uses.
 */
@SuppressWarnings("deprecation")
public final class TestEnchantments {
    public static final String NAMESPACE = "brainagehud_test";

    private static MappedRegistry<Enchantment> registry;

    private TestEnchantments() {}

    /** The bootstrapped, frozen registry; the game is bootstrapped on first use. */
    public static synchronized MappedRegistry<Enchantment> registry() {
        if (registry == null) registry = createRegistry();
        return registry;
    }

    public static Holder.Reference<Enchantment> enchantment(String path) {
        return registry().getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, identifier(path)));
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(NAMESPACE, path);
    }

    private static MappedRegistry<Enchantment> createRegistry() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        bindItemComponents();

        MappedRegistry<Enchantment> enchantments =
                new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());

        TagKey<Enchantment> damageExclusive = enchantmentTag("damage_exclusive");
        TagKey<Enchantment> maceExclusive = enchantmentTag("mace_exclusive");
        HolderSet<Enchantment> damageExclusiveSet = HolderSet.emptyNamed(enchantments, damageExclusive);
        HolderSet<Enchantment> maceExclusiveSet = HolderSet.emptyNamed(enchantments, maceExclusive);
        HolderSet<Enchantment> noExclusivity = HolderSet.empty();

        Holder.Reference<Enchantment> sharpness =
                register(enchantments, "sharpness", "Sharpness", 5, damageExclusiveSet, Items.DIAMOND_SWORD, Items.DIAMOND_AXE);
        Holder.Reference<Enchantment> smite =
                register(enchantments, "smite", "Smite", 5, damageExclusiveSet, Items.DIAMOND_SWORD);
        Holder.Reference<Enchantment> baneOfArthropods =
                register(enchantments, "bane_of_arthropods", "Bane of Arthropods", 5, damageExclusiveSet, Items.DIAMOND_SWORD);
        Holder.Reference<Enchantment> breach =
                register(enchantments, "breach", "Breach", 4, maceExclusiveSet, Items.MACE);
        Holder.Reference<Enchantment> density =
                register(enchantments, "density", "Density", 5, maceExclusiveSet, Items.MACE);
        register(enchantments, "wind_burst", "Wind Burst", 3, noExclusivity, Items.MACE);
        register(enchantments, "unbreaking", "Unbreaking", 3, noExclusivity, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE);
        register(enchantments, "mending", "Mending", 1, noExclusivity, Items.DIAMOND_SWORD, Items.DIAMOND_PICKAXE, Items.MACE);
        register(enchantments, "fire_aspect", "Fire Aspect", 2, noExclusivity, Items.DIAMOND_SWORD);
        register(enchantments, "looting", "Looting", 3, noExclusivity, Items.DIAMOND_SWORD);

        enchantments.bindTags(
                Map.of(
                        damageExclusive, List.of(sharpness, smite, baneOfArthropods),
                        maceExclusive, List.of(breach, density)));
        enchantments.freeze();

        return enchantments;
    }

    private static void bindItemComponents() {
        // vanilla binds item components while loading a world, so a headless test applies them
        // itself in order for ItemStack construction - and therefore isEnchantable/isEnchanted -
        // to behave as it does in game
        HolderLookup.Provider vanillaRegistries = VanillaRegistries.createLookup();

        for (DataComponentInitializers.PendingComponents<?> pendingComponents :
                BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(vanillaRegistries)) {
            pendingComponents.apply();
        }
    }

    private static Holder.Reference<Enchantment> register(
            MappedRegistry<Enchantment> enchantments,
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

    private static TagKey<Enchantment> enchantmentTag(String path) {
        return TagKey.create(Registries.ENCHANTMENT, identifier(path));
    }
}
