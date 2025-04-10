package io.github.brainage04.brainagehud.config.hud.custom.armour_info;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ArmourInfoHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;
    public boolean showArmour;
    public boolean showMainHand;
    public boolean showOffHand;
    public boolean showItemNames;
    public boolean showDurabilityBar;
    public DurabilityFormat durabilityFormat;
    public int durabilityDecimalPlaces;

    public ArmourInfoHudConfig() {
        this.coreSettings = new CoreSettings(0, "Armor Info HUD", true, -150, -5, ElementAnchor.BOTTOM_RIGHT);
        this.showArmour = true;
        this.showMainHand = false;
        this.showOffHand = false;
        this.showItemNames = false;
        this.showDurabilityBar = true;
        this.durabilityFormat = DurabilityFormat.ONE_NUMBER;
        this.durabilityDecimalPlaces = 1;
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
