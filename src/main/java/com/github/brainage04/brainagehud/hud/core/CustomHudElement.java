package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface CustomHudElement<T extends CoreSettingsContainer> extends HudElement<T> {
    void render(TextRenderer renderer, DrawContext drawContext);
}
