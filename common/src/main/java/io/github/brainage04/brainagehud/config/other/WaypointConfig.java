package io.github.brainage04.brainagehud.config.other;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class WaypointConfig {
    @ConfigEntry.Gui.Tooltip public boolean showInWorld = true;

    @ConfigEntry.Gui.Tooltip public boolean showWorldCentre = true;

    @ConfigEntry.ColorPicker public int worldCentreColour = 0xFFFFFF;

    @ConfigEntry.Gui.Tooltip public boolean showBeams = true;

    @ConfigEntry.Gui.Tooltip public boolean showMarkers = true;

    @ConfigEntry.Gui.Tooltip public boolean showLabels = true;

    @ConfigEntry.Gui.Tooltip public boolean alwaysShowNames = false;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 50, max = 200)
    public int labelScalePercent = 100;
}
