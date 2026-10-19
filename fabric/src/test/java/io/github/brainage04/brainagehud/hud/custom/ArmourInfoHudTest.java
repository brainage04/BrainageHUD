package io.github.brainage04.brainagehud.hud.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.brainage04.brainagehud.TestEnchantments;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.DurabilityFormat;
import java.text.DecimalFormatSymbols;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArmourInfoHudTest {
    @BeforeAll
    static void bootstrap() {
        TestEnchantments.registry();
    }

    @Test
    void formatsRemainingDurabilityRatherThanDamageTaken() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.set(DataComponents.MAX_DAMAGE, 200);
        sword.setDamageValue(50);
        ArmourInfoHudConfig config = new ArmourInfoHudConfig();

        assertEquals("150", ArmourInfoHud.generateItemInfo(sword, config));
        config.durabilityFormat = DurabilityFormat.BOTH_NUMBERS;
        config.showItemNames = true;
        assertEquals("Diamond Sword: 150 / 200", ArmourInfoHud.generateItemInfo(sword, config));
        config.durabilityFormat = DurabilityFormat.PERCENTAGE;
        assertEquals("Diamond Sword: 75" + DecimalFormatSymbols.getInstance().getDecimalSeparator() + "0%",
                ArmourInfoHud.generateItemInfo(sword, config));
    }

    @Test
    void hidingDurabilityDoesNotLeaveAPunctuationSuffix() {
        ArmourInfoHudConfig config = new ArmourInfoHudConfig();
        config.showItemNames = true;
        config.durabilityFormat = DurabilityFormat.NONE;

        assertEquals("Diamond Sword", ArmourInfoHud.generateItemInfo(new ItemStack(Items.DIAMOND_SWORD), config));
        config.showItemNames = false;
        assertEquals("", ArmourInfoHud.generateItemInfo(new ItemStack(Items.DIAMOND_SWORD), config));
    }

    @Test
    void nonDamageableItemsHaveOnlyTheirName() {
        ArmourInfoHudConfig config = new ArmourInfoHudConfig();
        config.showItemNames = true;
        config.durabilityFormat = DurabilityFormat.PERCENTAGE;

        assertEquals("Stone", ArmourInfoHud.generateItemInfo(new ItemStack(Items.STONE), config));
    }
}
