package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.FishingHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class FishingHud implements BasicCoreHudElement<FishingHudConfig> {
    private static final String OPEN_WATER = "Open water: true";
    private static final String NOT_OPEN_WATER = "Open water: false";
    private static final String TREASURE_UNAVAILABLE = "Treasure: unavailable";
    private static final String TREASURE_ELIGIBLE = "Treasure: eligible";
    private static final String TREASURE_INELIGIBLE = "Treasure: ineligible";

    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        LocalPlayer player = Minecraft.getInstance().player;
        FishingHook fishHook = player == null ? null : player.fishing;
        if (fishHook == null) {
            lines.add(TREASURE_UNAVAILABLE);
            return lines;
        }

        boolean openWater = fishHook.isOpenWaterFishing();
        lines.add(openWater ? OPEN_WATER : NOT_OPEN_WATER);
        lines.add(openWater ? TREASURE_ELIGIBLE : TREASURE_INELIGIBLE);
        return lines;
    }

    @Override
    public FishingHudConfig getElementConfig() {
        return getConfig().fishingHudConfig;
    }
}
