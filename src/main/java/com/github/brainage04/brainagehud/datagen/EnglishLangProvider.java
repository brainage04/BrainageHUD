package com.github.brainage04.brainagehud.datagen;

import com.github.brainage04.brainagehud.BrainageHUD;
import com.github.brainage04.brainagehud.util.LangUtils;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {

    public EnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private final String prefix = String.format("text.autoconfig.%s.option", BrainageHUD.MOD_ID);

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                String.format("text.autoconfig.%s.title", BrainageHUD.MOD_ID),
                "BrainageHUD Config Editor"
        );

        // keybinds
        translationBuilder.add(
                LangUtils.CONFIG_KEYBIND_KEY,
                "Open Config"
        );

        translationBuilder.add(
                LangUtils.GUI_KEYBIND_KEY,
                "Edit GUI"
        );

        // top-level settings
        String[] keys = new String[]{
                "primaryTextColour",
                "secondaryTextColour",
                "disabledTextColour",
                "highlightedElementColour",
                "elementPadding",
                "screenMargin",
                "textShadows",
                "backdrop",
                "backdropColour",
                "qualityOfLifeImprovementsConfig",
                "positionHudConfig",
                "dateTimeHudConfig",
                "toggleSprintHudConfig",
                "performanceHudConfig",
                "networkHudConfig",
        };
        String[] values = new String[]{
                "Primary Text Colour",
                "Secondary Text Colour",
                "Disabled Text Colour",
                "Highlighted Element Colour",
                "Padding",
                "Screen Margin",
                "Text Shadows",
                "Backdrop",
                "Backdrop Colour",
                "QOL Improvements Config",
                "Position HUD Config",
                "Date and Time HUD Config",
                "Toggle Sprint HUD Config",
                "Performance HUD Config",
                "Network HUD Config",
        };

        for (int i = 0; i < keys.length; i++) {
            translationBuilder.add(String.format("%s.%s", prefix, keys[i]), values[i]);
        }

        // core settings
        keys = new String[]{
                "positionHudConfig",
                "dateTimeHudConfig",
                "toggleSprintHudConfig",
                "performanceHudConfig",
                "networkHudConfig",
        };
        String[] suffixes = new String[]{
                "enabled",
                "x",
                "y",
                "elementAnchor",
        };
        values = new String[]{
                "Enabled",
                "X",
                "Y",
                "Anchor",
        };

        for (String s : keys) {
            String key = String.format("%s.%s.%s", prefix, s, "coreSettings");
            translationBuilder.add(key, "Core Settings");

            for (int j = 0; j < suffixes.length; j++) {
                translationBuilder.add(String.format("%s.%s", key, suffixes[j]), values[j]);
            }
        }

        // quality of live improvement config
        suffixes = new String[]{
                "gamma",
                "gamma.@Tooltip[0]",
                "gamma.@Tooltip[1]",
                "gamma.@Tooltip[2]",
                "gamma.@Tooltip[3]",

        };
        values = new String[]{
                "Gamma",
                "1: Maximum vanilla brightness.",
                "2: Very bright but lighting is still in effect.",
                "3: Fullbright.",
                "Update in-game with /updategamma (until I figure out how to update it automatically)",
        };

        for (int i = 0; i < suffixes.length; i++) {
            translationBuilder.add(String.format("%s.%s.%s", prefix, keys[0], suffixes[i]), values[i]);
        }

        // position hud config
        suffixes = new String[]{
                "showPosition",
                "positionDecimalPlaces",
                "cCounter",
                "eCounter",
                "showDirection",
                "showRotation",
                "rotationDecimalPlaces",
        };
        values = new String[]{
                "Show Position",
                "Decimal Places (Position)",
                "C Counter",
                "E Counter",
                "Show Direction",
                "Show Rotation",
                "Decimal Places (Rotation)",
        };

        for (int i = 0; i < suffixes.length; i++) {
            translationBuilder.add(String.format("%s.%s.%s", prefix, keys[1], suffixes[i]), values[i]);
        }

        // date time hud config
        suffixes = new String[]{
                "showDate",
                "showTime",
                "twelveHourFormat",
                "showTimezone",
        };
        values = new String[]{
                "Show Date",
                "Show Time",
                "Twelve Hour Format",
                "Show Timezone",
        };

        for (int i = 0; i < suffixes.length; i++) {
            translationBuilder.add(String.format("%s.%s.%s", prefix, keys[2], suffixes[i]), values[i]);
        }

        // performance hud config
        suffixes = new String[]{
                "showFps",
                "showRamUsage",
                "showCpuUsage",
                "showGpuUsage",
                "showGpuUsage.@Tooltip[0]",
                "showGpuUsage.@Tooltip[1]",
                "showGpuUsage.@Tooltip[2]",
        };
        values = new String[]{
                "Show FPS",
                "Show RAM Usage",
                "Show CPU Usage",
                "Show GPU Usage",
                "WARNING: This feature is currently broken.",
                "(GPU usage does not update unless the F3 screen is open)",
                "A fix for this will be implemented in the future."
        };

        for (int i = 0; i < suffixes.length; i++) {
            translationBuilder.add(String.format("%s.%s.%s", prefix, keys[3], suffixes[i]), values[i]);
        }

        // network hud config
        suffixes = new String[]{
                "showPing",
                "showTps",
                "tpsDecimalPlaces",
                "showNetworkActivity",
        };
        values = new String[]{
                "Show Ping",
                "Show TPS",
                "Decimal Places (TPS)",
                "Show Network Activity",
        };

        for (int i = 0; i < suffixes.length; i++) {
            translationBuilder.add(String.format("%s.%s.%s", prefix, keys[4], suffixes[i]), values[i]);
        }
    }
}
