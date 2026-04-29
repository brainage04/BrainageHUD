package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.FishingHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class FishingHud implements BasicCoreHudElement<FishingHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        Minecraft client = Minecraft.getInstance();
        if (client.getCameraEntity() == null) return lines;
        LocalPlayer player = client.player;
        if (player == null) return lines;
        FishingHook fishHook = player.fishing;
        if (fishHook == null) return lines;

        String message = Boolean.toString(fishHook.isOpenWaterFishing());

        lines.add("Open water: %s".formatted(message));

        return lines;
    }

    @Override
    public FishingHudConfig getElementConfig() {
        return getConfig().fishingHudConfig;
    }
}
