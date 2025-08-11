package io.github.brainage04.brainagehud.command.core;

import io.github.brainage04.brainagehud.command.FullbrightCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class ModCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            FullbrightCommand.initialize(dispatcher);
        }));
    }
}
