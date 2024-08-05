package com.github.brainage04.brainagehud.modmenu;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(BrainageHUDConfig.class, parent).get();
    }
}