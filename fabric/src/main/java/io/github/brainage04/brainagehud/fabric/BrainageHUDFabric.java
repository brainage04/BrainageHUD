package io.github.brainage04.brainagehud.fabric;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.command.FullbrightCommand;
import io.github.brainage04.brainagehud.command.core.ModCommands;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import io.github.brainage04.brainagehud.event.ModTooltipEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public final class BrainageHUDFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrainageHUD.initialize();
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> {
                    ModCommands.registerClientCommands(dispatcher, registryAccess);
                    FullbrightCommand.initialize(dispatcher);
                });
        ItemTooltipCallback.EVENT.register(
                (itemStack, context, tooltipType, lines) ->
                        ModTooltipEvents.onItemTooltip(itemStack, lines));
        ClientTickEvents.START_CLIENT_TICK.register(
                client -> {
                    ModTickEvents.onClientTick();
                    ModPacketEvents.onClientTick();
                });
    }
}
