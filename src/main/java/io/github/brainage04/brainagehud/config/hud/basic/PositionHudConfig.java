package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class PositionHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings(5, "Position HUD", true, 5, 5, ElementAnchor.TOP_LEFT);
    public boolean showPosition = true;
    public int positionDecimalPlaces = 1;
    public boolean showChunkPosition = true;
    public boolean cCounter = true;
    public boolean eCounter = true;
    public boolean showDirection = true;
    public boolean showRotation = true;
    public int rotationDecimalPlaces = 2;
    public boolean showLight = true;
    public boolean showBiome = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
