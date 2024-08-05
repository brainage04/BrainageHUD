package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;

public class ToggleSprintHUD {
    public static void toggleSprintHud(TextRenderer renderer, DrawContext drawContext, BrainageHUDConfig.ToggleSprintHUDConfig settings) {
        if (!settings.coreSettings.enabled) {
            return;
        }

        if (MinecraftClient.getInstance().player == null) {
            return;
        }

        List<String> lines = new ArrayList<>(List.of(Formatting.GRAY + "[Walking (Vanilla)]"));

        if (MinecraftClient.getInstance().player.isSprinting()) {
            if (MinecraftClient.getInstance().options.getSprintToggled().getValue() && MinecraftClient.getInstance().options.sprintKey.isPressed()) {
                lines.set(0, "[Sprinting (Toggled)]");
            } else {
                lines.set(0, "[Sprinting (Vanilla)]");
            }
        }

        if (MinecraftClient.getInstance().player.isSneaking()) {
            if (MinecraftClient.getInstance().options.getSneakToggled().getValue() && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
                lines.set(0, "[Sneaking (Toggled)]");
            } else {
                lines.set(0, "[Sneaking (Vanilla)]");
            }
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}