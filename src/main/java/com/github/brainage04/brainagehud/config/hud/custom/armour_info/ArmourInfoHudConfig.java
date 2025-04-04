package com.github.brainage04.brainagehud.config.hud.custom.armour_info;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import com.github.brainage04.brainagehud.config.core.ElementAnchor;

@SuppressWarnings("CanBeFinal")
public class ArmourInfoHudConfig extends CoreSettingsContainer {
    public boolean showArmour;
    public boolean showMainHand;
    public boolean showOffHand;
    public boolean showItemNames;
    public boolean showDurabilityBar;
    public DurabilityFormat durabilityFormat;
    public int durabilityDecimalPlaces;

    public ArmourInfoHudConfig() {
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
