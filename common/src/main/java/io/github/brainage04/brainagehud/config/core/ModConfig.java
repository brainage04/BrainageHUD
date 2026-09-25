package io.github.brainage04.brainagehud.config.core;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.hud.basic.*;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
import io.github.brainage04.brainagehud.config.other.EnchantInfoConfig;
import io.github.brainage04.brainagehud.config.other.QualityOfLifeConfig;
import io.github.brainage04.brainagehud.config.other.WaypointConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
@Config(name = BrainageHUD.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public QualityOfLifeConfig qualityOfLifeConfig = new QualityOfLifeConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EnchantInfoConfig enchantInfoConfig = new EnchantInfoConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public WaypointConfig waypointConfig = new WaypointConfig();

    // basic
    @ConfigEntry.Gui.CollapsibleObject
    public DateTimeHudConfig dateTimeHudConfig = new DateTimeHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EntityHudConfig entityHudConfig = new EntityHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public FishingHudConfig fishingHudConfig = new FishingHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public FoodHudConfig foodHudConfig = new FoodHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public MotionHudConfig motionHudConfig = new MotionHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public NetworkHudConfig networkHudConfig = new NetworkHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public PerformanceHudConfig performanceHudConfig = new PerformanceHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public PositionHudConfig positionHudConfig = new PositionHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public ProjectileHudConfig projectileHudConfig = new ProjectileHudConfig();

    @ConfigEntry.Gui.CollapsibleObject public ReachHudConfig reachHudConfig = new ReachHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public StatusEffectHudConfig statusEffectHudConfig = new StatusEffectHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public ToggleSprintHudConfig toggleSprintHudConfig = new ToggleSprintHudConfig();

    // custom
    @ConfigEntry.Gui.CollapsibleObject
    public ArmourInfoHudConfig armourInfoHudConfig = new ArmourInfoHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EnchantInfoHudConfig enchantInfoHudConfig = new EnchantInfoHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public KeystrokesHudConfig keystrokesHudConfig = new KeystrokesHudConfig();

    /**
     * Clamps hand-edited values into the ranges the config screen enforces, so that a bad
     * {@code brainagehud.json} cannot break rendering.
     */
    @Override
    public void validatePostLoad() {
        qualityOfLifeConfig.fullbright = Math.clamp(qualityOfLifeConfig.fullbright, -1.0F, 1.0F);

        networkHudConfig.updatePingTickInterval = Math.clamp(networkHudConfig.updatePingTickInterval, 1, 20);
        networkHudConfig.pingIntervalsTracked = Math.clamp(networkHudConfig.pingIntervalsTracked, 1, 30);
        networkHudConfig.tpsIntervalsTracked = Math.clamp(networkHudConfig.tpsIntervalsTracked, 1, 30);
        networkHudConfig.tpsDecimalPlaces = clampDecimalPlaces(networkHudConfig.tpsDecimalPlaces);

        positionHudConfig.positionDecimalPlaces = clampDecimalPlaces(positionHudConfig.positionDecimalPlaces);
        positionHudConfig.rotationDecimalPlaces = clampDecimalPlaces(positionHudConfig.rotationDecimalPlaces);
        reachHudConfig.decimalPlaces = clampDecimalPlaces(reachHudConfig.decimalPlaces);
        motionHudConfig.decimalPlaces = clampDecimalPlaces(motionHudConfig.decimalPlaces);
        armourInfoHudConfig.durabilityDecimalPlaces = clampDecimalPlaces(armourInfoHudConfig.durabilityDecimalPlaces);

        waypointConfig.labelScalePercent = Math.clamp(waypointConfig.labelScalePercent, 50, 200);
        keystrokesHudConfig.keyBackdropOpacity = Math.clamp(keystrokesHudConfig.keyBackdropOpacity, 0, 255);
    }

    private static int clampDecimalPlaces(int decimalPlaces) {
        return Math.clamp(decimalPlaces, 0, MathUtils.MAX_DECIMAL_PLACES);
    }
}
