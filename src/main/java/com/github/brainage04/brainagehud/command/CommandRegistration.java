package com.github.brainage04.brainagehud.command;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.BrainageHUDElementEditor;
import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class CommandRegistration {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(BrainageHUD.MOD_ID + "config")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> context.getSource().getClient().setScreen(
                                    AutoConfig.getConfigScreen(BrainageHUDConfig.class, context.getSource().getClient().currentScreen).get()
                            ));
                            return 1;
                        })
                )
        ));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(BrainageHUD.MOD_ID + "gui")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> context.getSource().getClient().setScreen(
                                    new BrainageHUDElementEditor()
                            ));
                            return 1;
                        })
                )
        ));
    }
}
