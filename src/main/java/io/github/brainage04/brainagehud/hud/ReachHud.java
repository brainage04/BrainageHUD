package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ReachHudConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ReachHud implements BasicCoreHudElement<ReachHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        Minecraft client = Minecraft.getInstance();
        if (client.getCameraEntity() == null) return lines;
        LocalPlayer player = client.player;
        if (player == null) return lines;
        ClientLevel world = client.level;
        if (world == null) return lines;

        HitResult hitResult = client.hitResult;
        if (hitResult == null) return lines;

        if (hitResult.getType() != HitResult.Type.MISS) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                Block block = world.getBlockState(blockHitResult.getBlockPos()).getBlock();

                if (block != null && block != Blocks.AIR && getElementConfig().showName) {
                    lines.add(block.getName().getString());

                    if (getElementConfig().showCoordinates) {
                        lines.add(blockHitResult.getBlockPos().toShortString());
                    }
                }
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                if (getElementConfig().showName) {
                    lines.add(entity.getName().getString());

                    if (getElementConfig().showCoordinates) {
                        lines.add(entity.blockPosition().toShortString());
                    }
                }
            }

            lines.add("%s blocks".formatted(
                    MathUtils.roundDecimalPlaces(client.player.getEyePosition().distanceTo(hitResult.getLocation()), getElementConfig().decimalPlaces))
            );
        }

        return lines;
    }

    @Override
    public ReachHudConfig getElementConfig() {
        return getConfig().reachHudConfig;
    }
}
