package io.github.brainage04.brainagehud.waypoint;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import net.minecraft.core.BlockPos;

/**
 * The built-in waypoint at 0, 63, 0 that every dimension has. It is not stored with the world's
 * waypoints: its colour and visibility live in the config, and it cannot be edited or deleted.
 */
public final class WorldCentre {
    public static final String NAME = "World Centre";
    public static final BlockPos POS = new BlockPos(0, 63, 0);

    private WorldCentre() {}

    /** The World Centre in {@code dimension}, with the configured colour and visibility. */
    public static Waypoint in(String dimension) {
        Waypoint waypoint = new Waypoint(NAME, POS, dimension, getConfig().waypointConfig.worldCentreColour);
        waypoint.visible = getConfig().waypointConfig.showWorldCentre;
        return waypoint;
    }

    public static void setVisible(boolean visible) {
        getConfig().waypointConfig.showWorldCentre = visible;
        ConfigUtils.saveConfig();
    }
}
