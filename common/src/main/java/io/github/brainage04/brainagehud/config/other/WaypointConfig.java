package io.github.brainage04.brainagehud.config.other;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class WaypointConfig {
    /** Draws the current dimension's visible waypoints in the world. */
    public boolean showInWorld = true;
    /** The built-in World Centre waypoint at 0, 63, 0 in every dimension; also toggled from the Waypoints screen. */
    public boolean showWorldCentre = true;
    @ConfigEntry.ColorPicker
    public int worldCentreColour = 0xFFFFFF;
    /** A beam through the whole height of the dimension, visible from far away. */
    public boolean showBeams = true;
    /** A floating, spinning gem above the waypoint, with a pulse on the ground below it when near. */
    public boolean showMarkers = true;
    /** The name and distance above the waypoint, readable through walls. */
    public boolean showLabels = true;
    /**
     * Shows every waypoint's name. Otherwise only the waypoint you look towards and waypoints
     * nearby show their names; the rest show just their distance.
     */
    public boolean alwaysShowNames = false;
    @ConfigEntry.BoundedDiscrete(min = 50, max = 200)
    public int labelScalePercent = 100;
}
