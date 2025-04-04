package com.github.brainage04.brainagehud.keybind;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.hud.core.HudElementEditor;
import com.github.brainage04.brainagehud.config.core.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeys {
    public static final KeyBinding openConfig = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "%s.openConfig".formatted(ModKeys.class.getPackageName()),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ENTER,
            BrainageHUD.MOD_NAME
    ));

    public static final KeyBinding openElementEditor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "%s.openElementEditor".formatted(ModKeys.class.getPackageName()),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            BrainageHUD.MOD_NAME
    ));

    public static void registerKeys() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openConfig.isPressed()) {
                MinecraftClient.getInstance().setScreen(
                        AutoConfig.getConfigScreen(ModConfig.class, MinecraftClient.getInstance().currentScreen).get()
                );
            }

            if (openElementEditor.isPressed()) {
                MinecraftClient.getInstance().setScreen(
                        new HudElementEditor()
                );
            }
        });
    }
}
