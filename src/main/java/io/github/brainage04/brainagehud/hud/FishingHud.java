package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.FishingHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class FishingHud implements BasicHudElement<FishingHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.cameraEntity == null) return lines;
        ClientPlayerEntity player = client.player;
        if (player == null) return lines;
        FishingBobberEntity fishHook = player.fishHook;
        if (fishHook == null) return lines;

        String message = Boolean.toString(fishHook.isInOpenWater());

        lines.add("Open water: %s".formatted(message));

        return lines;
    }

    @Override
    public FishingHudConfig getElementConfig() {
        return getConfig().fishingHudConfig;
    }
}