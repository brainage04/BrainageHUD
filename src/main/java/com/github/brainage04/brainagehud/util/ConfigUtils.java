package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import me.shedaniel.autoconfig.AutoConfig;

public class ConfigUtils {
    public static BrainageHUDConfig getConfig() {
        return AutoConfig.getConfigHolder(BrainageHUDConfig.class).getConfig();
    }
}
