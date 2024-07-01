package com.github.brainage04.brainagehud.keybind;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.BrainageHUDElementEditor;
import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.util.LangUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindRegistration {
    public static KeyBinding configKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            LangUtils.CONFIG_KEYBIND_KEY,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ENTER,
            BrainageHUD.MOD_NAME
    ));

    public static KeyBinding guiKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            LangUtils.GUI_KEYBIND_KEY,
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_KP_ADD,
            BrainageHUD.MOD_NAME
    ));

    public static void registerKeybinds() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (configKeybind.isPressed()) {
                MinecraftClient.getInstance().setScreen(
                        AutoConfig.getConfigScreen(BrainageHUDConfig.class, MinecraftClient.getInstance().currentScreen).get()
                );
            }

            if (guiKeybind.isPressed()) {
                MinecraftClient.getInstance().setScreen(
                        new BrainageHUDElementEditor()
                );
            }
        });
    }
}
