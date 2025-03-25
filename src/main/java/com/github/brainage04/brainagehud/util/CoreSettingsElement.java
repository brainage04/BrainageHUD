package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.core.CoreSettings;

public class CoreSettingsElement {
    public ElementCorners corners;
    public final CoreSettings coreSettings;

    public CoreSettingsElement(ElementCorners corners, CoreSettings coreSettings) {
        this.corners = corners;
        this.coreSettings = coreSettings;
    }
}
