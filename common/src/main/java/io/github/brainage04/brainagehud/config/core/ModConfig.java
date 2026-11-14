package io.github.brainage04.brainagehud.config.core;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.hud.basic.DateTimeHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.EntityHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.FishingHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.FoodHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.MotionHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.PerformanceHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.PositionHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.ProjectileHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.ReachHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.StatusEffectHudConfig;
import io.github.brainage04.brainagehud.config.hud.basic.ToggleSprintHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
import io.github.brainage04.brainagehud.config.other.EnchantInfoConfig;
import io.github.brainage04.brainagehud.config.other.QualityOfLifeConfig;
import io.github.brainage04.brainagehud.config.other.WaypointConfig;
import io.github.brainage04.brainagehud.util.MathUtils;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.util.Utils;

@SuppressWarnings("CanBeFinal")
@Config(name = BrainageHUD.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject
    public QualityOfLifeConfig qualityOfLifeConfig = new QualityOfLifeConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EnchantInfoConfig enchantInfoConfig = new EnchantInfoConfig();

    @ConfigEntry.Gui.CollapsibleObject public WaypointConfig waypointConfig = new WaypointConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public DateTimeHudConfig dateTimeHudConfig = new DateTimeHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EntityHudConfig entityHudConfig = new EntityHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public FishingHudConfig fishingHudConfig = new FishingHudConfig();

    @ConfigEntry.Gui.CollapsibleObject public FoodHudConfig foodHudConfig = new FoodHudConfig();

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

    @ConfigEntry.Gui.CollapsibleObject
    public ArmourInfoHudConfig armourInfoHudConfig = new ArmourInfoHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public EnchantInfoHudConfig enchantInfoHudConfig = new EnchantInfoHudConfig();

    @ConfigEntry.Gui.CollapsibleObject
    public KeystrokesHudConfig keystrokesHudConfig = new KeystrokesHudConfig();

    @Override
    public void validatePostLoad() {
        restoreMissingSections();

        qualityOfLifeConfig.fullbright = Math.clamp(qualityOfLifeConfig.fullbright, -1.0F, 1.0F);

        networkHudConfig.updatePingTickInterval =
                Math.clamp(networkHudConfig.updatePingTickInterval, 1, 20);
        networkHudConfig.pingIntervalsTracked =
                Math.clamp(networkHudConfig.pingIntervalsTracked, 1, 30);
        networkHudConfig.tpsIntervalsTracked =
                Math.clamp(networkHudConfig.tpsIntervalsTracked, 1, 30);
        networkHudConfig.tpsDecimalPlaces = clampDecimalPlaces(networkHudConfig.tpsDecimalPlaces);

        positionHudConfig.positionDecimalPlaces =
                clampDecimalPlaces(positionHudConfig.positionDecimalPlaces);
        positionHudConfig.rotationDecimalPlaces =
                clampDecimalPlaces(positionHudConfig.rotationDecimalPlaces);
        reachHudConfig.decimalPlaces = clampDecimalPlaces(reachHudConfig.decimalPlaces);
        motionHudConfig.decimalPlaces = clampDecimalPlaces(motionHudConfig.decimalPlaces);
        armourInfoHudConfig.durabilityDecimalPlaces =
                clampDecimalPlaces(armourInfoHudConfig.durabilityDecimalPlaces);

        waypointConfig.labelScalePercent = Math.clamp(waypointConfig.labelScalePercent, 50, 200);
        keystrokesHudConfig.keyBackdropOpacity =
                Math.clamp(keystrokesHudConfig.keyBackdropOpacity, 0, 255);

        // Gson reads a format name it does not know as null.
        if (keystrokesHudConfig.clicksPerSecondFormat == null) {
            keystrokesHudConfig.clicksPerSecondFormat =
                    new KeystrokesHudConfig().clicksPerSecondFormat;
        }
        if (armourInfoHudConfig.durabilityFormat == null) {
            armourInfoHudConfig.durabilityFormat = new ArmourInfoHudConfig().durabilityFormat;
        }

        armourInfoHudConfig.coreSettings =
                renamedFrom103(
                        armourInfoHudConfig.coreSettings,
                        "Armor Info HUD",
                        () -> new ArmourInfoHudConfig().coreSettings);
        dateTimeHudConfig.coreSettings =
                renamedFrom103(
                        dateTimeHudConfig.coreSettings,
                        "Date Time HUD",
                        () -> new DateTimeHudConfig().coreSettings);
    }

    /**
     * Gson reads a section the file sets to {@code null} as null. On the first load AutoConfig runs
     * this method before HudRendererLib's load listener could restore an element section.
     */
    private void restoreMissingSections() {
        ModConfig defaults = null;
        for (Field field : ModConfig.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Utils.getUnsafely(field, this) != null) {
                continue;
            }
            if (defaults == null) defaults = new ModConfig();
            BrainageHUD.LOGGER.warn(
                    "Config value ModConfig.{} is missing, using the default", field.getName());
            Utils.setUnsafely(field, this, Utils.getUnsafely(field, defaults));
        }
    }

    /**
     * {@code settings} with the current default name when the file still has the name {@code
     * oldName} that BrainageHUD 1.0.3 saved; a name the player chose stays.
     */
    private static CoreSettings renamedFrom103(
            CoreSettings settings, String oldName, Supplier<CoreSettings> defaults) {
        if (settings == null || !oldName.equals(settings.elementName)) return settings;

        CoreSettings renamed =
                new CoreSettings(
                        defaults.get().elementName,
                        settings.enabled,
                        settings.x,
                        settings.y,
                        settings.elementAnchor);
        renamed.elementOverrides = settings.elementOverrides;
        if (settings.elementId != CoreSettingsIdAssigner.INVALID_ID) {
            renamed.elementId = settings.elementId;
        }
        return renamed;
    }

    private static int clampDecimalPlaces(int decimalPlaces) {
        return Math.clamp(decimalPlaces, 0, MathUtils.MAX_DECIMAL_PLACES);
    }
}
