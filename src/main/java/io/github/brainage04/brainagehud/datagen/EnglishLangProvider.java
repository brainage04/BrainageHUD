package io.github.brainage04.brainagehud.datagen;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.util.StringUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {
    public EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
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
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                "text.autoconfig.%s.title".formatted(BrainageHUD.MOD_ID),
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
    }
}
