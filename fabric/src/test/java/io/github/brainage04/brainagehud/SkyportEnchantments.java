package io.github.brainage04.brainagehud;

import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * The enchantments an iron sword can take on the Skyport server, as its Enchant Info panel listed
 * them on 9 Nov 2026: the vanilla sword enchantments plus Skyport's own. The damage ones
 * (Sharpness, Smite, Bane of Arthropods and Skyport's Annihilation, Banes and Duelist) exclude each
 * other, and so do Cataclysm and Volatile.
 *
 * <p>They are registered in identifier order (path first, as {@link Identifier#compareTo} sorts),
 * the order a server loads datapack enchantments in, so the missing list comes out in the order the
 * panel showed.
 */
@SuppressWarnings("deprecation")
public final class SkyportEnchantments {
    public static final List<String> DAMAGE =
            List.of(
                    "minecraft:sharpness",
                    "minecraft:smite",
                    "minecraft:bane_of_arthropods",
                    "skyport:annihilation",
                    "skyport:bane_of_illagers",
                    "skyport:bane_of_the_deep",
                    "skyport:bane_of_the_end",
                    "skyport:bane_of_the_nether",
                    "skyport:duelist");

    public static final List<String> CATACLYSM = List.of("skyport:cataclysm", "skyport:volatile");

    public static final List<String> OTHERS =
            List.of(
                    "minecraft:fire_aspect",
                    "minecraft:knockback",
                    "minecraft:looting",
                    "minecraft:mending",
                    "minecraft:sweeping_edge",
                    "minecraft:unbreaking",
                    "skyport:blinding",
                    "skyport:bloodlust",
                    "skyport:crippling",
                    "skyport:dread",
                    "skyport:drifting",
                    "skyport:eclipse",
                    "skyport:enfeebling",
                    "skyport:famine",
                    "skyport:infestation",
                    "skyport:insight",
                    "skyport:oozing",
                    "skyport:plague",
                    "skyport:pyre",
                    "skyport:sapping",
                    "skyport:siphon",
                    "skyport:soulbound",
                    "skyport:stormcaller",
                    "skyport:updraft",
                    "skyport:venom",
                    "skyport:vertigo",
                    "skyport:wardens_wrath",
                    "skyport:warding",
                    "skyport:withering");

    private static MappedRegistry<Enchantment> registry;

    private SkyportEnchantments() {}

    public static synchronized MappedRegistry<Enchantment> registry() {
        if (registry == null) registry = createRegistry();
        return registry;
    }

    /** The name an identifier's path reads as: {@code wardens_wrath} is "Warden's Wrath". */
    public static String name(Identifier id) {
        if (id.getPath().equals("wardens_wrath")) return "Warden's Wrath";
        StringBuilder name = new StringBuilder();
        for (String word : id.getPath().split("_")) {
            if (!name.isEmpty()) name.append(' ');
            boolean minor = !name.isEmpty() && List.of("of", "the").contains(word);
            name.append(
                    minor
                            ? word
                            : word.substring(0, 1).toUpperCase(Locale.ROOT) + word.substring(1));
        }
        return name.toString();
    }

    private static MappedRegistry<Enchantment> createRegistry() {
        // bootstraps the game and binds the item components
        TestEnchantments.registry();

        MappedRegistry<Enchantment> enchantments =
                new MappedRegistry<>(Registries.ENCHANTMENT, Lifecycle.stable());
        TagKey<Enchantment> damage = tag("exclusive_set/damage");
        TagKey<Enchantment> cataclysm = tag("exclusive_set/cataclysm");
        HolderSet<Enchantment> damageSet = HolderSet.emptyNamed(enchantments, damage);
        HolderSet<Enchantment> cataclysmSet = HolderSet.emptyNamed(enchantments, cataclysm);

        List<Identifier> ids = new ArrayList<>();
        for (List<String> group : List.of(DAMAGE, CATACLYSM, OTHERS)) {
            group.forEach(id -> ids.add(Identifier.parse(id)));
        }
        ids.sort(Comparator.naturalOrder());

        List<Holder<Enchantment>> damageMembers = new ArrayList<>();
        List<Holder<Enchantment>> cataclysmMembers = new ArrayList<>();
        for (Identifier id : ids) {
            boolean inDamage = DAMAGE.contains(id.toString());
            boolean inCataclysm = CATACLYSM.contains(id.toString());
            Enchantment enchantment =
                    new Enchantment(
                            Component.literal(name(id)),
                            Enchantment.definition(
                                    HolderSet.direct(
                                            BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_SWORD)),
                                    10,
                                    3,
                                    Enchantment.constantCost(1),
                                    Enchantment.constantCost(21),
                                    1,
                                    EquipmentSlotGroup.MAINHAND),
                            inDamage ? damageSet : inCataclysm ? cataclysmSet : HolderSet.empty(),
                            DataComponentMap.EMPTY);
            Holder.Reference<Enchantment> holder =
                    enchantments.register(
                            ResourceKey.create(Registries.ENCHANTMENT, id),
                            enchantment,
                            RegistrationInfo.BUILT_IN);
            if (inDamage) damageMembers.add(holder);
            if (inCataclysm) cataclysmMembers.add(holder);
        }

        enchantments.bindTags(Map.of(damage, damageMembers, cataclysm, cataclysmMembers));
        enchantments.freeze();
        return enchantments;
    }

    private static TagKey<Enchantment> tag(String path) {
        return TagKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace(path));
    }
}
