package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.core.ModConfig;
import com.github.brainage04.brainagehud.config.hud.custom.ClicksPerSecondFormat;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;

public class ConfigUtils {
    public static void initialize() {
        AutoConfig.getConfigHolder(ModConfig.class).registerSaveListener((configHolder, modConfig) -> {
            MinecraftClient.getInstance().options.getGamma().setValue(
                    modConfig.qualityOfLifeImprovementsConfig.gamma
            );

            RenderUtils.populateCoreSettingsElements();

            return ActionResult.SUCCESS;
        });
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
        getConfig().qualityOfLifeImprovementsConfig.gamma = value;
        saveConfig();
    }

    public static boolean isLeftClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == ClicksPerSecondFormat.LEFT_CLICK
                || getConfig().keystrokesHudConfig.clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }

    public static boolean isRightClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == ClicksPerSecondFormat.RIGHT_CLICK
                || getConfig().keystrokesHudConfig.clicksPerSecondFormat == ClicksPerSecondFormat.BOTH;
    }
}
