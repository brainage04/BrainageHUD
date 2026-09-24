package io.github.brainage04.brainagehud.neoforge;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.command.core.ModCommands;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import io.github.brainage04.brainagehud.event.ModTooltipEvents;
import io.github.brainage04.brainagehud.waypoint.WaypointRenderer;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = BrainageHUD.MOD_ID, dist = Dist.CLIENT)
public final class BrainageHUDNeoForge {
    public BrainageHUDNeoForge(ModContainer container) {
        BrainageHUD.initialize();
        container.registerExtensionPoint(
                IConfigScreenFactory.class,
                (modContainer, parent) -> AutoConfigClient.getConfigScreen(ModConfig.class, parent).get());
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> ModTickEvents.onClientTick());
        NeoForge.EVENT_BUS.addListener(
                (ItemTooltipEvent event) ->
                        ModTooltipEvents.onItemTooltip(event.getItemStack(), event.getToolTip()));
        NeoForge.EVENT_BUS.addListener(
                (RegisterClientCommandsEvent event) ->
                        ModCommands.registerClientCommands(event.getDispatcher(), event.getBuildContext()));
        NeoForge.EVENT_BUS.addListener(
                (SubmitCustomGeometryEvent event) -> WaypointRenderer.submit(
                        event.getPoseStack(), event.getSubmitNodeCollector(), Minecraft.getInstance().gameRenderer.mainCamera()));
    }
}
