package com.github.brainage04.brainagehud.datagen;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.config.ModConfig;
import com.github.brainage04.brainagehud.util.StringUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {

    public EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private final String autoConfigPrefix = String.format("text.autoconfig.%s.option", BrainageHUD.MOD_ID);

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

    private void addAutoConfigTranslations(String[] keys, String[] values, String packageName, TranslationBuilder translationBuilder) {
        for (int i = 0; i < keys.length; i++) {
            translationBuilder.add("%s.%s.%s".formatted(autoConfigPrefix, packageName, keys[i]), values[i]);
        }
    }

    private void addAutomaticTranslations(String[] keys, String packageName, TranslationBuilder translationBuilder) {
        for (String key : keys) {
            translationBuilder.add("%s.%s.%s".formatted(packageName, BrainageHUD.MOD_ID, key), StringUtils.pascalCaseToHumanReadable(key));
        }
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                String.format("text.autoconfig.%s.title", BrainageHUD.MOD_ID),
                "BrainageHUD Config Editor"
        );

        // keybinds
        addAutomaticTranslations(
                new String[]{
                        "openConfigEditor",
                        "openElementEditor"
                },
                "keybind",
                translationBuilder
        );

        // config
        generateReflectedTranslations(ModConfig.class, autoConfigPrefix, translationBuilder);

        // quality of life improvement config
        addAutoConfigTranslations(
                new String[]{
                        "gamma.@Tooltip[0]",
                        "gamma.@Tooltip[1]",
                        "gamma.@Tooltip[2]",
                        "gamma.@Tooltip[3]",
                },
                new String[]{
                        "1.0: Maximum vanilla brightness.",
                        "2.0: Very bright but lighting is still in effect.",
                        "3.0: Fullbright.",
                        "Update in-game with /setgamma <value> (until I figure out how to update it automatically)",
                },
                "qualityOfLifeImprovementsConfig",
                translationBuilder
        );

        // performance hud config
        addAutoConfigTranslations(
                new String[]{
                        "showGpuUsage.@Tooltip[0]",
                        "showGpuUsage.@Tooltip[1]",
                        "showGpuUsage.@Tooltip[2]",
                },
                new String[]{
                        "WARNING: This feature is currently broken.",
                        "(GPU usage does not update unless the F3 screen is open)",
                        "A fix for this will be implemented in the future."
                },
                "performanceHudConfig",
                translationBuilder
        );
    }
}
