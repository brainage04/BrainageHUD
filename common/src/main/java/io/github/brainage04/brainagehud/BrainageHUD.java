package io.github.brainage04.brainagehud;

import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.hud.*;
import io.github.brainage04.brainagehud.hud.custom.ArmourInfoHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.hud.custom.KeystrokesHud;
import io.github.brainage04.brainagehud.keys.ModKeys;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrainageHUD {
    public static final String MOD_ID = "brainagehud";
    public static final String MOD_NAME = "BrainageHUD";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile boolean initialized;

    public static void initialize() {
        LOGGER.info(MOD_NAME + " initialising...");

        HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

        HudRendererLib.registerConfigCommand(ModConfig.class, MOD_ID);
        HudRendererLib.registerConfigKey(
                ModConfig.class, GLFW.GLFW_KEY_KP_SUBTRACT, MOD_ID, MOD_NAME);
        ModKeys.initialize();

        HudRendererLib.registerHudElement(new ArmourInfoHud());
        HudRendererLib.registerHudElement(new EnchantInfoHud());
        HudRendererLib.registerHudElement(new KeystrokesHud());

        HudRendererLib.registerHudElement(new DateTimeHud());
        HudRendererLib.registerHudElement(new EntityHud());
        HudRendererLib.registerHudElement(new FishingHud());
        HudRendererLib.registerHudElement(new FoodHud());
        HudRendererLib.registerHudElement(new MotionHud());
        HudRendererLib.registerHudElement(new NetworkHud());
        HudRendererLib.registerHudElement(new PerformanceHud());
        HudRendererLib.registerHudElement(new PositionHud());
        HudRendererLib.registerHudElement(new ProjectileHud());
        HudRendererLib.registerHudElement(new ReachHud());
        HudRendererLib.registerHudElement(new ToggleSprintHud());

        initialized = true;

        LOGGER.info(MOD_NAME + " initialised.");
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
