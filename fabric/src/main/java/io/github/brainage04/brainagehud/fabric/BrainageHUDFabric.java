package io.github.brainage04.brainagehud.fabric;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.command.core.ModCommands;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class BrainageHUDFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrainageHUD.initialize();
        ModCommands.initialize();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ModTickEvents.onClientTick();
            ModPacketEvents.onClientTick();
        });
    }
}
