package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.ToggleSprintHUDConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.hud.core.HUDRenderer.renderElement;

public class ToggleSprintHUD {
    public static void render(TextRenderer renderer, DrawContext drawContext, ToggleSprintHUDConfig settings) {
        if (!settings.coreSettings.enabled) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        List<String> lines = new ArrayList<>(List.of(Formatting.GRAY + "[Walking (Vanilla)]"));

        if (player.isSprinting()) {
            if (MinecraftClient.getInstance().options.getSprintToggled().getValue() && MinecraftClient.getInstance().options.sprintKey.isPressed()) {
                lines.set(0, "[Sprinting (Toggled)]");
            } else {
                lines.set(0, "[Sprinting (Vanilla)]");
            }
        }

        if (player.isSneaking()) {
            if (MinecraftClient.getInstance().options.getSneakToggled().getValue() && MinecraftClient.getInstance().options.sneakKey.isPressed()) {
                lines.set(0, "[Sneaking (Toggled)]");
            } else {
                lines.set(0, "[Sneaking (Vanilla)]");
            }
        }

        renderElement(renderer, drawContext, lines, settings.coreSettings);
    }
}