package io.github.brainage04.brainagehud.waypoint;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** A user-created waypoint in one dimension of one world. Serialised with Gson. */
public final class Waypoint {
    public String name;
    public int x;
    public int y;
    public int z;

    public String dimension;

    public int colour;

    public boolean visible = true;

    public Waypoint(String name, BlockPos pos, String dimension, int colour) {
        this.name = name;
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        this.dimension = dimension;
        this.colour = colour;
    }

    public BlockPos pos() {
        return new BlockPos(x, y, z);
    }

    public Vec3 standingPosition() {
        return new Vec3(x + 0.5D, y, z + 0.5D);
    }
}
