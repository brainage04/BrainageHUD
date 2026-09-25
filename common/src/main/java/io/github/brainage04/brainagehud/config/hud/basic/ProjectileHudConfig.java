package io.github.brainage04.brainagehud.config.hud.basic;

import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ProjectileHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings = new CoreSettings("Projectile HUD", true, -5, -5, ElementAnchor.BOTTOM_RIGHT);
    public boolean showArrows = true;
    public boolean showSnowballs = true;
    public boolean showEggs = true;
    public boolean showEnderPearls = true;
    public boolean showWindCharges = true;
    @ConfigEntry.Gui.Tooltip public boolean showSlotCounts = false;

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }

    @Override
    public void setCoreSettings(CoreSettings coreSettings) {
        this.coreSettings = coreSettings;
    }
}
