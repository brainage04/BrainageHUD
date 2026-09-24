package io.github.brainage04.brainagehud.datagen;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.util.StringUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {
    public EnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private final String autoConfigPrefix = "text.autoconfig.%s.option".formatted(BrainageHUD.MOD_ID);

    private void generateReflectedTranslations(Class<?> clazz, String baseKey, TranslationBuilder translationBuilder) {
        for (Field field : clazz.getFields()) {
            String newBaseKey = "%s.%s".formatted(baseKey, field.getName());

            translationBuilder.add(newBaseKey, StringUtils.pascalCaseToHumanReadable(field.getName()));

            if (field.getType().isPrimitive()) continue;
            if (field.getType().isEnum()) continue;
            if (field.getType() == String.class) continue;

            generateReflectedTranslations(field.getType(), newBaseKey, translationBuilder);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAutomaticTranslations(String[] keys, String packageName, TranslationBuilder translationBuilder) {
        for (String key : keys) {
            translationBuilder.add("%s.%s.%s".formatted(packageName, BrainageHUD.MOD_ID, key), StringUtils.pascalCaseToHumanReadable(key));
        }
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                "text.autoconfig.%s.title".formatted(BrainageHUD.MOD_ID),
                "BrainageHUD Config Editor"
        );

        // keybinds
        addAutomaticTranslations(
                new String[]{
                        "openConfig",
                        "createWaypoint",
                        "manageWaypoints",
                        "inventoryStats"
                },
                "key",
                translationBuilder
        );
        translationBuilder.add("key.category.%s.keys".formatted(BrainageHUD.MOD_ID), BrainageHUD.MOD_NAME);

        // config
        generateReflectedTranslations(ModConfig.class, autoConfigPrefix, translationBuilder);

        // tooltips
        String[][] tooltips = {
                {"motionHudConfig.showBlocksPerTick", "Also show the speed in blocks per tick."},
                {"entityHudConfig.showWaterCreatures", "Fish, squid, dolphins, axolotls and other water mobs."},
                {"entityHudConfig.showAmbient", "Bats."},
                {"entityHudConfig.showOthers", "Everything that is not a mob: players, items, projectiles, armour stands, vehicles and so on."},
                {"projectileHudConfig.showSlotCounts", "When the items are spread over several slots, also list the count in each slot (hotbar left to right, then the inventory rows, then the off hand)."},
                {"foodHudConfig.showSlotCounts", "When the items are spread over several slots, also list the count in each slot (hotbar left to right, then the inventory rows, then the off hand)."},
                {"positionHudConfig.showTrueYaw", "When your yaw has wound past ±180°, also show the raw value the game stores."},
                {"toggleSprintHudConfig.showInternalValues", "Also show the game's toggle setting and the key's state for sprint and sneak, for debugging."},
                {"positionHudConfig.rotationOnlyWithFarmingTool", "Only show the rotation numbers while you hold an axe, a hoe or a Hypixel SkyBlock farming tool."},
                {"networkHudConfig.colourValues", "Colour ping and TPS from dark green (good) to dark red (bad)."},
        };
        for (String[] tooltip : tooltips) {
            translationBuilder.add("%s.%s.@Tooltip".formatted(autoConfigPrefix, tooltip[0]), tooltip[1]);
        }
    }
}
