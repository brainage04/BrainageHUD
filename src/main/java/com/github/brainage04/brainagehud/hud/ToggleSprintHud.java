package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.basic.ToggleSprintHudConfig;
import com.github.brainage04.brainagehud.hud.core.BasicHudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ToggleSprintHud implements BasicHudElement<ToggleSprintHudConfig> {
    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>();

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return lines;

        if (player.isSprinting()) {
            if (MinecraftClient.getInstance().options.getSprintToggled().getValue() && MinecraftClient.getInstance().options.sprintKey.isPressed()) {
                lines.add("[Sprinting (Toggled)]");
            } else {
                lines.add("[Sprinting (Vanilla)]");
            }
        }

        if (player.isSneaking()) {
            if (MinecraftClient.getInstance().options.getSneakToggled().getValue() && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
                lines.add("[Sneaking (Toggled)]");
            } else {
                lines.add("[Sneaking (Vanilla)]");
            }
        }

        if (lines.isEmpty()) {
            lines.add(Formatting.GRAY + "[Walking (Vanilla)]");
        }

        return lines;
    }

    @Override
    public ToggleSprintHudConfig getElementConfig() {
        return getConfig().toggleSprintHudConfig;
    }
}