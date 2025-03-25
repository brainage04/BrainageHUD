package com.github.brainage04.brainagehud.config;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

public class QualityOfLifeImprovements {
    @ConfigEntry.Gui.Tooltip(count = 4)
    public double gamma;

    public QualityOfLifeImprovements() {
        this.gamma = 3;
    }
}
