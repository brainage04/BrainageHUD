package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.basic.ReachHudConfig;
import com.github.brainage04.brainagehud.hud.core.BasicHudElement;
import com.github.brainage04.brainagehud.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ReachHud implements BasicHudElement<ReachHudConfig> {
    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>(List.of());

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
                }
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                if (getElementConfig().showName) {
                    lines.add(entity.getName().getString());
                }
            }

            lines.add("%s blocks".formatted(MathUtils.roundDecimalPlaces(client.player.getEyePos().distanceTo(hitResult.getPos()), getElementConfig().decimalPlaces)));
        }

        return lines;
    }

    @Override
    public ReachHudConfig getElementConfig() {
        return getConfig().reachHudConfig;
    }
}