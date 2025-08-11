package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ToggleSprintHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ToggleSprintHud implements BasicCoreHudElement<ToggleSprintHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

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
            lines.add(Text.literal("[Walking (Vanilla)]").formatted(Formatting.GRAY));
        }

        return lines;
    }

    @Override
    public ToggleSprintHudConfig getElementConfig() {
        return getConfig().toggleSprintHudConfig;
    }
}