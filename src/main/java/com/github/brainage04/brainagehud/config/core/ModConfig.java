package com.github.brainage04.brainagehud.config.core;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.config.hud.basic.*;
import com.github.brainage04.brainagehud.config.other.QualityOfLifeConfig;
import com.github.brainage04.brainagehud.config.hud.custom.armour_info.ArmourInfoHudConfig;
import com.github.brainage04.brainagehud.config.hud.custom.keystrokes.KeystrokesHudConfig;
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

    @ConfigEntry.Gui.CollapsibleObject public ArmourInfoHudConfig armourInfoHudConfig = new ArmourInfoHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public KeystrokesHudConfig keystrokesHudConfig = new KeystrokesHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public DateTimeHudConfig dateTimeHudConfig = new DateTimeHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public NetworkHudConfig networkHudConfig = new NetworkHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public PerformanceHudConfig performanceHudConfig = new PerformanceHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public PositionHudConfig positionHudConfig = new PositionHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public ReachHudConfig reachHudConfig = new ReachHudConfig();
    @ConfigEntry.Gui.CollapsibleObject public ToggleSprintHudConfig toggleSprintHudConfig = new ToggleSprintHudConfig();
}
