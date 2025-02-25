package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;

public class ConfigUtils {
    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
        updateGamma();
    }

    public static void loadConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).load();
    }

    public static void updateGamma() {
        MinecraftClient.getInstance().options.getGamma().setValue(getConfig().qualityOfLifeImprovementsConfig.gamma);
    }

    public static void setGamma(double value) {
        getConfig().qualityOfLifeImprovementsConfig.gamma = value;
        saveConfig();
    }

    public static boolean isLeftClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == ModConfig.ClicksPerSecondFormat.LEFT_CLICK || getConfig().keystrokesHudConfig.clicksPerSecondFormat == ModConfig.ClicksPerSecondFormat.BOTH;
    }

    public static boolean isRightClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == ModConfig.ClicksPerSecondFormat.RIGHT_CLICK || getConfig().keystrokesHudConfig.clicksPerSecondFormat == ModConfig.ClicksPerSecondFormat.BOTH;
    }
}
