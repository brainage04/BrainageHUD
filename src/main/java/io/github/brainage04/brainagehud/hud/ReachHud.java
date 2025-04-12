package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ReachHudConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ReachHud implements BasicHudElement<ReachHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity == null) return lines;
        ClientPlayerEntity player = client.player;
        if (player == null) return lines;
        ClientWorld world = client.world;
        if (world == null) return lines;

        HitResult hitResult = client.gameRenderer.findCrosshairTarget(
                client.cameraEntity,
                client.player.getBlockInteractionRange(),
                client.player.getEntityInteractionRange(),
                0
        );

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
                        lines.add(entity.getBlockPos().toShortString());
                    }
                }
            }

            lines.add("%s blocks".formatted(
                    MathUtils.roundDecimalPlaces(client.player.getEyePos().distanceTo(hitResult.getPos()), getElementConfig().decimalPlaces))
            );
        }

        return lines;
    }

    @Override
    public ReachHudConfig getElementConfig() {
        return getConfig().reachHudConfig;
    }
}