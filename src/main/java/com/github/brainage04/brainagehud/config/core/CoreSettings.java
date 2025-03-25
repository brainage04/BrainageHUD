package com.github.brainage04.brainagehud.config.core;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class CoreSettings {
    @ConfigEntry.Gui.Excluded public final int elementId;
    @ConfigEntry.Gui.Excluded public final String elementName;

    public boolean enabled;
    public int x;
    public int y;
    public ElementAnchor elementAnchor;

    public boolean overrideGlobalBackdropOpacity;
    @ConfigEntry.BoundedDiscrete(min = 0, max = 255)
    public int backdropOpacity;

    public CoreSettings(int elementId, String elementName, boolean enabled, int x, int y, ElementAnchor elementAnchor, boolean overrideGlobalBackdropOpacity, int backdropOpacity) {
        this.elementId = elementId;
        this.elementName = elementName;

        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.elementAnchor = elementAnchor;

        this.overrideGlobalBackdropOpacity = overrideGlobalBackdropOpacity;
        this.backdropOpacity = backdropOpacity;
    }
}
