package com.github.brainage04.brainagehud.config.core;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.config.other.QualityOfLifeConfig;
import com.github.brainage04.brainagehud.config.hud.*;
import com.github.brainage04.brainagehud.config.hud.custom.ArmourInfoHUDConfig;
import com.github.brainage04.brainagehud.config.hud.custom.KeystrokesHUDConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
@Config(name = BrainageHUD.MOD_ID)
public class ModConfig implements ConfigData {
    public boolean textShadows = true;
    @ConfigEntry.ColorPicker public int primaryTextColour = 0xffffff;

    @ConfigEntry.BoundedDiscrete(min = 0, max = 255) public int backdropOpacity = 0;

    public int elementPadding = 2;
    public int screenMargin = 5;

    @ConfigEntry.Gui.Tooltip()
    public boolean adjustTopRightElementsWithStatusEffects = true;
    public int adjustTopRightElementsWithStatusEffectsAmount = 21;

    @ConfigEntry.Gui.CollapsibleObject public QualityOfLifeConfig qualityOfLifeConfig = new QualityOfLifeConfig();

    @ConfigEntry.Gui.CollapsibleObject public ArmourInfoHUDConfig armourInfoHudConfig = new ArmourInfoHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public KeystrokesHUDConfig keystrokesHudConfig = new KeystrokesHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public DateTimeHUDConfig dateTimeHudConfig = new DateTimeHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public NetworkHUDConfig networkHudConfig = new NetworkHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public PerformanceHUDConfig performanceHudConfig = new PerformanceHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public PositionHUDConfig positionHudConfig = new PositionHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public ReachHUDConfig reachHUDConfig = new ReachHUDConfig();
    @ConfigEntry.Gui.CollapsibleObject public ToggleSprintHUDConfig toggleSprintHudConfig = new ToggleSprintHUDConfig();
}
