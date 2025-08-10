package io.github.brainage04.brainagehud.config.core;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.hud.basic.*;
import io.github.brainage04.brainagehud.config.other.QualityOfLifeConfig;
import io.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import io.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
@Config(name = BrainageHUD.MOD_ID)
public class ModConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject public QualityOfLifeConfig qualityOfLifeConfig = new QualityOfLifeConfig();

    // basic
    @ConfigEntry.Gui.CollapsibleObject public DateTimeHudConfig dateTimeHudConfig = new DateTimeHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public FishingHudConfig fishingHudConfig = new FishingHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public NetworkHudConfig networkHudConfig = new NetworkHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public PerformanceHudConfig performanceHudConfig = new PerformanceHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public PositionHudConfig positionHudConfig = new PositionHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public ReachHudConfig reachHudConfig = new ReachHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public ToggleSprintHudConfig toggleSprintHudConfig = new ToggleSprintHudConfig();

    // custom
    @ConfigEntry.Gui.CollapsibleObject public ArmourInfoHudConfig armourInfoHudConfig = new ArmourInfoHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public KeystrokesHudConfig keystrokesHudConfig = new KeystrokesHudConfig();
}
