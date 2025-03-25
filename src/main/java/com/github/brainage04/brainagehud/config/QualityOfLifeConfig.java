package com.github.brainage04.brainagehud.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class QualityOfLifeConfig {
    @ConfigEntry.Gui.Tooltip(count = 4)
    public double gamma;

    public QualityOfLifeConfig() {
        this.gamma = 3;
    }
}
