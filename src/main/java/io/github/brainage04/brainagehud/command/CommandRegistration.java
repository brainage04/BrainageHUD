package io.github.brainage04.brainagehud.command;

import io.github.brainage04.brainagehud.util.ConfigUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class CommandRegistration {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("fullbright")
                        .executes(context -> {
                            toggleFullbright();

                            String message = getConfig().qualityOfLifeConfig.fullbright ? "enabled" : "disabled";

                            context.getSource().sendFeedback(Text.literal("Fullbright %s.".formatted(message)));

                            return 1;
                        })
                )
        ));
    }

    public static void toggleFullbright() {
        getConfig().qualityOfLifeConfig.fullbright = !getConfig().qualityOfLifeConfig.fullbright;
        ConfigUtils.saveConfig();
    }
}
