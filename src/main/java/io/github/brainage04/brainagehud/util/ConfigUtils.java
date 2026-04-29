package io.github.brainage04.brainagehud.util;

import io.github.brainage04.brainagehud.config.core.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class ConfigUtils {
    public static ModConfig getConfig() {
        return AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }
}
