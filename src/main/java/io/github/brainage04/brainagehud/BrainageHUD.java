package io.github.brainage04.brainagehud;

import io.github.brainage04.brainagehud.command.CommandRegistration;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import io.github.brainage04.brainagehud.hud.*;
import io.github.brainage04.brainagehud.hud.custom.ArmourInfoHud;
import io.github.brainage04.brainagehud.hud.custom.KeystrokesHud;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrainageHUD implements ClientModInitializer {
	public static final String MOD_ID = "brainagehud";
	public static final String MOD_NAME = "BrainageHUD";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
	public void onInitializeClient() {
		LOGGER.info(MOD_NAME + " initialising...");

		HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

		CommandRegistration.registerCommands();
		HudRendererLib.registerConfigCommand(ModConfig.class, MOD_ID);
		HudRendererLib.registerConfigKey(ModConfig.class, GLFW.GLFW_KEY_KP_SUBTRACT, MOD_ID, MOD_NAME);

		HudRendererLib.registerHudElement(new ArmourInfoHud());
		HudRendererLib.registerHudElement(new KeystrokesHud());

		HudRendererLib.registerHudElement(new DateTimeHud());
		HudRendererLib.registerHudElement(new FishingHud());
		HudRendererLib.registerHudElement(new NetworkHud());
		HudRendererLib.registerHudElement(new PerformanceHud());
		HudRendererLib.registerHudElement(new PositionHud());
		HudRendererLib.registerHudElement(new ReachHud());
		HudRendererLib.registerHudElement(new ToggleSprintHud());

		ModTickEvents.initialize();
		ModPacketEvents.initialize();

		LOGGER.info(MOD_NAME + " initialised.");
	}
}