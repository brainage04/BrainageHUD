package io.github.brainage04.brainagehud.config.other;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class EnchantInfoConfig {
    @ConfigEntry.Gui.Tooltip public boolean highlightMaxLevelEnchants = true;

    public List<String> blacklistedEnchantmentIds =
            new ArrayList<>(
                    List.of(
                            "minecraft:binding_curse",
                            "minecraft:vanishing_curse",
                            "minecraft:blast_protection",
                            "minecraft:projectile_protection",
                            "minecraft:fire_protection",
                            "minecraft:thorns",
                            "minecraft:bane_of_arthropods",
                            "minecraft:smite",
                            "minecraft:knockback",
                            "minecraft:frost_walker"));
}
