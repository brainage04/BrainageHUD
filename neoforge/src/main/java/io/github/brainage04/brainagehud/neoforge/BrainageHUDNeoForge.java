package io.github.brainage04.brainagehud.neoforge;

import com.mojang.brigadier.arguments.FloatArgumentType;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = BrainageHUD.MOD_ID, dist = Dist.CLIENT)
public final class BrainageHUDNeoForge {
    public BrainageHUDNeoForge(IEventBus modBus) {
        BrainageHUD.initialize();
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
            ModTickEvents.onClientTick();
            ModPacketEvents.onClientTick();
        });
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("fullbright")
                .then(Commands.argument("amount", FloatArgumentType.floatArg(-1, 1))
                        .executes(context -> {
                            float value = FloatArgumentType.getFloat(context, "amount");
                            ConfigUtils.getConfig().qualityOfLifeConfig.fullbright = value;
                            ConfigUtils.saveConfig();
                            context.getSource().sendSuccess(() -> Component.literal("Fullbright set to %f.".formatted(value)), false);
                            return 1;
                        })));
    }
}
