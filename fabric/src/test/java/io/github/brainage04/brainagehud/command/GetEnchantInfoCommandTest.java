package io.github.brainage04.brainagehud.command;

import static io.github.brainage04.brainagehud.TestEnchantments.enchantment;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.TestEnchantments;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import org.junit.jupiter.api.Test;

class GetEnchantInfoCommandTest {
    @Test
    void matchesNamesRegardlessOfCase() {
        assertEquals(List.of(enchantment("sharpness")), matches("Sharpness"));
        assertEquals(List.of(enchantment("fire_aspect")), matches("FIRE ASPECT"));
    }

    @Test
    void listsEveryPartialMatchInRegistryOrder() {
        assertEquals(
                List.of(enchantment("unbreaking"), enchantment("mending"), enchantment("looting")),
                matches("ING"));
    }

    @Test
    void findsNothingForAnUnknownName() {
        assertEquals(List.of(), matches("protection"));
    }

    private static List<Holder.Reference<Enchantment>> matches(String query) {
        return GetEnchantInfoCommand.findMatches(TestEnchantments.registry(), query);
    }
}
