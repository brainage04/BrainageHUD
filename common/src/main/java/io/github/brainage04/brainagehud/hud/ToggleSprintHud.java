package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ToggleSprintHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ToggleSprintHud implements BasicCoreHudElement<ToggleSprintHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return lines;

        if (player.isSprinting()) {
            if (Minecraft.getInstance().options.toggleSprint().get() && Minecraft.getInstance().options.keySprint.isDown()) {
                lines.add("[Sprinting (Toggled)]");
            } else {
                lines.add("[Sprinting (Vanilla)]");
            }
        }

        if (player.isShiftKeyDown()) {
            if (Minecraft.getInstance().options.toggleCrouch().get() && Minecraft.getInstance().options.keyShift.isDown()) {
                lines.add("[Sneaking (Toggled)]");
            } else {
                lines.add("[Sneaking (Vanilla)]");
            }
        }

        if (lines.isEmpty()) {
            lines.add(Component.literal("[Walking (Vanilla)]").withStyle(ChatFormatting.GRAY));
        }

        return lines;
    }

    @Override
    public ToggleSprintHudConfig getElementConfig() {
        return getConfig().toggleSprintHudConfig;
    }
}