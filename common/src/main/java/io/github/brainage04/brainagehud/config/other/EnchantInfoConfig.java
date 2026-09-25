package io.github.brainage04.brainagehud.config.other;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

/**
 * Enchantment information settings ported from GetEnchantInfo's {@code config/getenchantinfo.json}.
 *
 * <p>The blacklist defaults are unchanged from GetEnchantInfo. Only the storage location differs:
 * these options now live in BrainageHUD's {@code config/brainagehud.json}, so a standalone
 * GetEnchantInfo config file is not read.
 */
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
