package io.github.brainage04.brainagehud;

import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.config.hud.basic.StatusEffectHudConfig;
import io.github.brainage04.brainagehud.hud.DateTimeHud;
import io.github.brainage04.brainagehud.hud.EntityHud;
import io.github.brainage04.brainagehud.hud.FishingHud;
import io.github.brainage04.brainagehud.hud.FoodHud;
import io.github.brainage04.brainagehud.hud.MotionHud;
import io.github.brainage04.brainagehud.hud.NetworkHud;
import io.github.brainage04.brainagehud.hud.PerformanceHud;
import io.github.brainage04.brainagehud.hud.PositionHud;
import io.github.brainage04.brainagehud.hud.ProjectileHud;
import io.github.brainage04.brainagehud.hud.ReachHud;
import io.github.brainage04.brainagehud.hud.StatusEffectHud;
import io.github.brainage04.brainagehud.hud.ToggleSprintHud;
import io.github.brainage04.brainagehud.hud.custom.ArmourInfoHud;
import io.github.brainage04.brainagehud.hud.custom.EnchantInfoHud;
import io.github.brainage04.brainagehud.hud.custom.KeystrokesHud;
import io.github.brainage04.brainagehud.keys.ModKeys;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrainageHUD {
    public static final String MOD_ID = "brainagehud";
    public static final String MOD_NAME = "Brainage HUD";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile boolean initialized;

    public static void initialize() {
        LOGGER.info(MOD_NAME + " initialising...");

        HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

        HudRendererLib.registerConfigCommand(ModConfig.class, MOD_ID);
        HudRendererLib.registerConfigKey(
                ModConfig.class, SDLScancode.SDL_SCANCODE_KP_MINUS, MOD_ID, MOD_NAME);
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
        HudRendererLib.registerHudElement(new StatusEffectHud());
        HudRendererLib.registerHudElement(new ToggleSprintHud());
        HudRendererLib.registerVanillaStatusEffectsHider(
                () -> {
                    StatusEffectHudConfig config = ConfigUtils.getConfig().statusEffectHudConfig;
                    return config.coreSettings.enabled && config.hideVanillaStatusEffects;
                });

        initialized = true;

        LOGGER.info(MOD_NAME + " initialised.");
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
