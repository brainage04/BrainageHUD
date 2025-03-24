package com.github.brainage04.brainagehud.command;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.hud.core.HUDElementEditor;
import com.github.brainage04.brainagehud.config.core.ModConfig;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static com.github.brainage04.brainagehud.util.ConfigUtils.setGamma;

public class CommandRegistration {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(BrainageHUD.MOD_ID + "config")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> context.getSource().getClient().setScreen(
                                    AutoConfig.getConfigScreen(ModConfig.class, context.getSource().getClient().currentScreen).get()
                            ));
                            return 1;
                        })
                )
        ));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(BrainageHUD.MOD_ID + "gui")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> context.getSource().getClient().setScreen(
                                    new HUDElementEditor()
                            ));
                            return 1;
                        })
                )
        ));

        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("setgamma")
                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg(0.0, 3.0))
                                .executes(context -> {
                                    final double value = DoubleArgumentType.getDouble(context, "value");
                                    setGamma(value);
                                    context.getSource().sendFeedback(Text.literal("Gamma set to %f.".formatted(value)));
                                    return 1;
                                })
                        )
                )
        ));
    }
}
