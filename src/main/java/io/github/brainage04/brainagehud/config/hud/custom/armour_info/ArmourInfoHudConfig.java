package io.github.brainage04.brainagehud.config.hud.custom.armour_info;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ArmourInfoHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Armor Info HUD", true, -150, -5, ElementAnchor.BOTTOM_RIGHT);
    public boolean showArmour = true;
    public boolean showMainHand = false;
    public boolean showOffHand = false;
    public boolean showItemNames = false;
    public boolean showDurabilityBar = true;
    public DurabilityFormat durabilityFormat = DurabilityFormat.FIRST_NUMBER;
    public int durabilityDecimalPlaces = 1;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
