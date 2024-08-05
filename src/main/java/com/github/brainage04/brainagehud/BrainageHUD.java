package com.github.brainage04.brainagehud;

import com.github.brainage04.brainagehud.command.CommandRegistration;
import com.github.brainage04.brainagehud.config.BrainageHUDConfig;
import com.github.brainage04.brainagehud.hud.core.HUDRenderer;
import com.github.brainage04.brainagehud.keybind.KeybindRegistration;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrainageHUD implements ClientModInitializer {
	public static final String MOD_ID = "brainagehud";
	public static final String MOD_NAME = "BrainageHUD";
	public static final String MOD_AUTHOR = "brainage04";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info(MOD_NAME + " initialising...");

		AutoConfig.register(BrainageHUDConfig.class, GsonConfigSerializer::new);

		HudRenderCallback.EVENT.register(new HUDRenderer());

		CommandRegistration.registerCommands();
		KeybindRegistration.registerKeybinds();

		LOGGER.info(MOD_NAME + " initialised.");
	}
}