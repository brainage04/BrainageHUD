package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PositionHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showPosition;
    public int positionDecimalPlaces;
    public boolean cCounter;
    public boolean eCounter;
    public boolean showDirection;
    public boolean showRotation;
    public int rotationDecimalPlaces;

    public PositionHudConfig() {
        this.coreSettings = new CoreSettings(5, "Position HUD", true, 5, 5, ElementAnchor.TOP_LEFT);
        this.showPosition = true;
        this.positionDecimalPlaces = 1;
        this.cCounter = true;
        this.eCounter = true;
        this.showDirection = true;
        this.showRotation = true;
        this.rotationDecimalPlaces = 2;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
