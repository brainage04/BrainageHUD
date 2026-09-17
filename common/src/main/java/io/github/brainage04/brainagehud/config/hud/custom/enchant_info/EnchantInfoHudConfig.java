package io.github.brainage04.brainagehud.config.hud.custom.enchant_info;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class EnchantInfoHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings =
            new CoreSettings("Enchant Info HUD", true, 5, 0, ElementAnchor.LEFT);

    public boolean showItemName = true;
    public boolean showEnchantments = true;
    public boolean showMaxLevels = true;
    public boolean showMissingEnchantments = true;
    public boolean showMissingHeader = true;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
