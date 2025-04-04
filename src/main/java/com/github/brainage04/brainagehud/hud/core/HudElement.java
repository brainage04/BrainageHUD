package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;

public interface HudElement<T extends CoreSettingsContainer> {
    T getElementConfig();
}
