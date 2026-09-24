package io.github.brainage04.brainagehud.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.command.core.ModCommands;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import io.github.brainage04.brainagehud.event.ModTooltipEvents;
import io.github.brainage04.brainagehud.waypoint.WaypointRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;

public final class BrainageHUDFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BrainageHUD.initialize();
        ClientCommandRegistrationCallback.EVENT.register(ModCommands::registerClientCommands);
        ItemTooltipCallback.EVENT.register(
                (itemStack, context, tooltipType, lines) ->
                        ModTooltipEvents.onItemTooltip(itemStack, lines));
        ClientTickEvents.START_CLIENT_TICK.register(client -> ModTickEvents.onClientTick());
        // submits are camera-relative; the view rotation is applied when they are drawn
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> WaypointRenderer.submit(
                new PoseStack(), context.submitNodeCollector(), Minecraft.getInstance().gameRenderer.mainCamera()));
    }
}
