package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.core.ModConfig;
import com.github.brainage04.brainagehud.hud.core.HudElementEditor;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public class ConfigUtils {
    @SuppressWarnings({"SameReturnValue", "unused"})
    private static ActionResult saveLoad(ConfigHolder<ModConfig> configHolder, ModConfig modConfig) {
        MinecraftClient.getInstance().options.getGamma().setValue(
                modConfig.qualityOfLifeConfig.gamma
        );

        HudElementEditor.populateCoreSettingsElements();

        return ActionResult.SUCCESS;
    }

    public static void initialize() {
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener(ConfigUtils::saveLoad);
        AutoConfig.getConfigHolder(ModConfig.class).registerLoadListener(ConfigUtils::saveLoad);
    }

    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }

    public static void loadConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).load();
    }

    public static void setGamma(double value) {
        getConfig().qualityOfLifeConfig.gamma = value;
        saveConfig();
    }
}
