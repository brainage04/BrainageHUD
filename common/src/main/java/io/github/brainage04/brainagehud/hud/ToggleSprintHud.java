package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.ToggleSprintHudConfig;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class ToggleSprintHud implements BasicCoreHudElement<ToggleSprintHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return lines;

        if (getElementConfig().showInternalValues) {
            Options options = minecraft.options;
            lines.add(internalValues("Sprint", options.toggleSprint().get(), options.keySprint));
            lines.add(internalValues("Sneak", options.toggleCrouch().get(), options.keyShift));
        }

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

    /** The game's own toggle setting and the key's state, which stays down while toggled on. */
    private static Component internalValues(String name, boolean toggle, KeyMapping key) {
        return Component.literal("%s: toggle %s, key down %s".formatted(name, toggle, key.isDown()))
                .withStyle(ChatFormatting.GRAY);
    }

    @Override
    public ToggleSprintHudConfig getElementConfig() {
        return getConfig().toggleSprintHudConfig;
    }
}