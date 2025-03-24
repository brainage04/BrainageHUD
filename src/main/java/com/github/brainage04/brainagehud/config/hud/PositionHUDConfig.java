package com.github.brainage04.brainagehud.config.hud;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PositionHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showPosition;
    public int positionDecimalPlaces;
    public boolean cCounter;
    public boolean eCounter;
    public boolean showDirection;
    public boolean showRotation;
    public int rotationDecimalPlaces;

    public PositionHUDConfig() {
        this.coreSettings = new CoreSettings(5, "Position HUD", true, 5, 5, ElementAnchor.TOP_LEFT, false, 100);
        this.showPosition = true;
        this.positionDecimalPlaces = 1;
        this.cCounter = true;
        this.eCounter = true;
        this.showDirection = true;
        this.showRotation = true;
        this.rotationDecimalPlaces = 2;
    }
}
