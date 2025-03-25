package com.github.brainage04.brainagehud.config.hud.custom;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ArmourInfoHUDConfig {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showArmour;
    public boolean showMainHand;
    public boolean showOffHand;
    public boolean showItemNames;
    public boolean showDurabilityBar;
    public DurabilityFormat durabilityFormat;
    public int durabilityDecimalPlaces;

    public ArmourInfoHUDConfig() {
        this.coreSettings = new CoreSettings(0, "Armor Info HUD", true, -150, -5, ElementAnchor.BOTTOM_RIGHT, false, 100);
        this.showArmour = true;
        this.showMainHand = false;
        this.showOffHand = false;
        this.showItemNames = false;
        this.showDurabilityBar = true;
        this.durabilityFormat = DurabilityFormat.ONE_NUMBER;
        this.durabilityDecimalPlaces = 1;
    }
}
