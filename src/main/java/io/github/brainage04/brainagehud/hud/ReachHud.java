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
    private TextList cachedLines = new TextList();
    private boolean attackWasDown;
    private boolean wasClickMode;

    @Override
    public TextList getLines() {
        ReachHudConfig config = getElementConfig();
        if (!config.updateOnAttackClick) {
            wasClickMode = false;
            attackWasDown = false;
            return calculateLines(config);
        }

        boolean attackDown = Minecraft.getInstance().options.keyAttack.isDown();
        if (!wasClickMode || attackDown && !attackWasDown) {
            cachedLines = calculateLines(config);
        }

        attackWasDown = attackDown;
        wasClickMode = true;
        return cachedLines;
    }

    private TextList calculateLines(ReachHudConfig config) {
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

                if (block != null && block != Blocks.AIR && config.showName) {
                    lines.add(block.getName().getString());

                    if (config.showCoordinates) {
                        lines.add(blockHitResult.getBlockPos().toShortString());
                    }
                }
            } else if (hitResult.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                Entity entity = entityHitResult.getEntity();

                if (config.showName) {
                    lines.add(entity.getName().getString());

                    if (config.showCoordinates) {
                        lines.add(entity.blockPosition().toShortString());
                    }
                }
            }

            lines.add("%s blocks".formatted(
                    MathUtils.roundDecimalPlaces(player.getEyePosition().distanceTo(hitResult.getLocation()), config.decimalPlaces))
            );
        }

        return lines;
    }

    @Override
    public ReachHudConfig getElementConfig() {
        return getConfig().reachHudConfig;
    }
}
