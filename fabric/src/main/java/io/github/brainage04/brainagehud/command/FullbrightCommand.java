package io.github.brainage04.brainagehud.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;

public class FullbrightCommand {
    public static int execute(FabricClientCommandSource source, float newValue) {
        getConfig().qualityOfLifeConfig.fullbright = newValue;
        ConfigUtils.saveConfig();

        source.sendFeedback(Component.literal("Fullbright set to %f.".formatted(newValue)));

        return 1;
    }

    public static void initialize(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("fullbright")
                .then(argument("amount", FloatArgumentType.floatArg(-1, 1))
                        .executes(context ->
                                execute(
                                        context.getSource(),
                                        FloatArgumentType.getFloat(context, "amount")
                                )
                        )
                )
        );
    }
}
