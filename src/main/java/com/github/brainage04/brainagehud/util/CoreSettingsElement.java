package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.core.CoreSettings;

import java.lang.reflect.Field;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class CoreSettingsElement {
    public ElementCorners corners;
    public final Field field;
    public final Field subfield;
    public final CoreSettings coreSettings;

    public CoreSettingsElement(ElementCorners corners, Field field, Field subfield, CoreSettings coreSettings) {
        this.corners = corners;
        this.field = field;
        this.subfield = subfield;
        this.coreSettings = coreSettings;
    }

    public void updateCoreSettings() {
        try {
            subfield.set(field.get(getConfig()), coreSettings);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
