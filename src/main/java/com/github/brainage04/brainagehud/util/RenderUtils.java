package com.github.brainage04.brainagehud.util;

import com.github.brainage04.brainagehud.config.core.CoreSettings;
import com.github.brainage04.brainagehud.config.core.ModConfig;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class RenderUtils {
    public static final List<CoreSettingsElement> CORE_SETTINGS_ELEMENTS = new ArrayList<>();

    public static void populateCoreSettingsElements() {
        CORE_SETTINGS_ELEMENTS.clear();

        ModConfig config = getConfig();

        for (Field field : config.getClass().getFields()) {
            if (field.getType().isPrimitive()) continue;

            for (Field subfield : field.getType().getFields()) {
                if (subfield.getType() != CoreSettings.class) continue;

                try {
                    CORE_SETTINGS_ELEMENTS.add(new CoreSettingsElement(new ElementCorners(), field, subfield, (CoreSettings) subfield.get(field.get(config))));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // todo: what is bro waffling on about
    // could be related to that alignment bug in the readme?
    public static ElementCorners getCornersWithPadding(int left, int top, int right, int bottom) {
        return new ElementCorners(
                left - getConfig().elementPadding * 2,
                top - getConfig().elementPadding * 2,
                right + getConfig().elementPadding * 2,
                bottom
        );
    }

    public static boolean mouseInRect(int x1, int y1, int x2, int y2, double mouseX, double mouseY) {
        return (x1 <= mouseX && mouseX <= x2 && y1 <= mouseY && mouseY <= y2);
    }

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }
    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}
