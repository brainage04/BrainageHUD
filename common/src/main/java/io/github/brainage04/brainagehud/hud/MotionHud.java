package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.MotionHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

/**
 * How fast the camera entity is moving, from how far it moved over the last tick. Unlike its
 * delta movement, this is what actually happened after collisions, friction and vehicles.
 */
public class MotionHud implements BasicCoreHudElement<MotionHudConfig> {
    private static final double TICKS_PER_SECOND = 20.0D;

    @Override
    public TextList getLines() {
        TextList lines = new TextList();
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (entity == null) return lines;

        MotionHudConfig config = getElementConfig();
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;

        if (config.showAxes) {
            lines.add(speed("Motion X", dx, config));
            lines.add(speed("Motion Y", dy, config));
            lines.add(speed("Motion Z", dz, config));
        }
        if (config.showHorizontalSpeed) {
            lines.add(speed("Speed", Math.sqrt(dx * dx + dz * dz), config));
        }

        return lines;
    }

    static String speed(String label, double blocksPerTick, MotionHudConfig config) {
        String line = "%s: %s m/s".formatted(label, roundDecimalPlaces(blocksPerTick * TICKS_PER_SECOND, config.decimalPlaces));
        if (config.showBlocksPerTick) line += " (%s m/tick)".formatted(roundDecimalPlaces(blocksPerTick, config.decimalPlaces));
        return line;
    }

    @Override
    public MotionHudConfig getElementConfig() {
        return getConfig().motionHudConfig;
    }
}
