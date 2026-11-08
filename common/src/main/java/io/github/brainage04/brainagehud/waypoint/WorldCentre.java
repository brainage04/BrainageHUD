package io.github.brainage04.brainagehud.waypoint;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import net.minecraft.core.BlockPos;

public final class WorldCentre {
    public static final String NAME = "World Centre";
    public static final BlockPos POS = new BlockPos(0, 63, 0);

    private WorldCentre() {}

    public static Waypoint in(String dimension) {
        Waypoint waypoint =
                new Waypoint(NAME, POS, dimension, getConfig().waypointConfig.worldCentreColour);
        waypoint.visible = getConfig().waypointConfig.showWorldCentre;
        return waypoint;
    }

    public static void setVisible(boolean visible) {
        getConfig().waypointConfig.showWorldCentre = visible;
        ConfigUtils.saveConfig();
    }
}
