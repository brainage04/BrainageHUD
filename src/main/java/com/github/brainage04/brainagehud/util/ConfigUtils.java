package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;

public class ConfigUtils {
    public static BrainageHUDConfig getConfig() {
        return AutoConfig.getConfigHolder(BrainageHUDConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(BrainageHUDConfig.class).save();
        updateGamma();
    }

    public static void loadConfig() {
        AutoConfig.getConfigHolder(BrainageHUDConfig.class).load();
    }

    public static void updateGamma() {
        MinecraftClient.getInstance().options.getGamma().setValue(getConfig().qualityOfLifeImprovementsConfig.gamma);
    }

    public static void setGamma(double value) {
        getConfig().qualityOfLifeImprovementsConfig.gamma = value;
        saveConfig();
    }

    public static boolean isLeftClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == BrainageHUDConfig.ClicksPerSecondFormat.LEFT_CLICK || getConfig().keystrokesHudConfig.clicksPerSecondFormat == BrainageHUDConfig.ClicksPerSecondFormat.BOTH;
    }

    public static boolean isRightClickShown() {
        return getConfig().keystrokesHudConfig.clicksPerSecondFormat == BrainageHUDConfig.ClicksPerSecondFormat.RIGHT_CLICK || getConfig().keystrokesHudConfig.clicksPerSecondFormat == BrainageHUDConfig.ClicksPerSecondFormat.BOTH;
    }
}
