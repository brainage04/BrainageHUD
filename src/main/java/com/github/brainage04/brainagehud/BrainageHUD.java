package com.github.brainage04.brainagehud;

import com.github.brainage04.brainagehud.command.CommandRegistration;
import com.github.brainage04.brainagehud.config.core.ModConfig;
import com.github.brainage04.brainagehud.event.ModPacketEvents;
import com.github.brainage04.brainagehud.event.ModTickEvents;
import com.github.brainage04.brainagehud.hud.*;
import com.github.brainage04.brainagehud.hud.core.HudRenderer;
import com.github.brainage04.brainagehud.hud.custom.ArmourInfoHud;
import com.github.brainage04.brainagehud.hud.custom.KeystrokesHud;
import com.github.brainage04.brainagehud.keybind.ModKeys;
import com.github.brainage04.brainagehud.util.ConfigUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrainageHUD implements ClientModInitializer {
	public static final String MOD_ID = "brainagehud";
	public static final String MOD_NAME = "BrainageHUD";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Identifier HUD_LAYER_0 = Identifier.of(MOD_ID, "hud-layer-0");

	@Override
	public void onInitializeClient() {
		LOGGER.info(MOD_NAME + " initialising...");

		AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
		ConfigUtils.initialize();

		ModTickEvents.initialize();
		ModPacketEvents.initialize();

		// hud
		HudLayerRegistrationCallback.EVENT.register(layeredDrawer ->
				layeredDrawer.attachLayerBefore(
						IdentifiedLayer.CHAT,
						HUD_LAYER_0,
						(context, tickCounter) ->
								HudRenderer.render(context)
				)
		);

		HudRenderer.registerHudElement(new ArmourInfoHud());
		HudRenderer.registerHudElement(new DateTimeHud());
		HudRenderer.registerHudElement(new KeystrokesHud());
		HudRenderer.registerHudElement(new NetworkHud());
		HudRenderer.registerHudElement(new PerformanceHud());
		HudRenderer.registerHudElement(new PositionHud());
		HudRenderer.registerHudElement(new ReachHud());
		HudRenderer.registerHudElement(new ToggleSprintHud());

		CommandRegistration.registerCommands();
		ModKeys.registerKeys();

		LOGGER.info(MOD_NAME + " initialised.");
	}
}