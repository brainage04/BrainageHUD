package com.github.brainage04.brainagehud.hud.core;

import com.github.brainage04.brainagehud.config.core.CoreSettingsContainer;

import java.util.List;

public interface BasicHudElement<T extends CoreSettingsContainer> extends HudElement<T> {
    List<String> getLines();
}
