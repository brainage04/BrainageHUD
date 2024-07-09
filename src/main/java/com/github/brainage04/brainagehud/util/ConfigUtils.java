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
}
