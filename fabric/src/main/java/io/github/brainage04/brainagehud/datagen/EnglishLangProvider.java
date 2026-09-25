package io.github.brainage04.brainagehud.datagen;

import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.core.ModConfig;
import io.github.brainage04.brainagehud.util.StringUtils;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EnglishLangProvider extends FabricLanguageProvider {
    public EnglishLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    private final String autoConfigPrefix = "text.autoconfig.%s.option".formatted(BrainageHUD.MOD_ID);

    /** Titles of the config sections that are not HUD elements; those are titled with their element name. */
    private static final Map<String, String> SECTION_TITLES = Map.of(
            "qualityOfLifeConfig", "Quality of Life",
            "enchantInfoConfig", "Enchant Info",
            "waypointConfig", "Waypoints"
    );

    /** Labels of the core settings that differ from their field names. */
    private static final Map<String, String> CORE_SETTINGS_LABELS = Map.of(
            "x", "X Coordinate",
            "y", "Y Coordinate",
            "elementOverrides", "Style Overrides"
    );

    private void generateReflectedTranslations(Object config, String baseKey, TranslationBuilder translationBuilder) {
        for (Field field : config.getClass().getFields()) {
            String newBaseKey = "%s.%s".formatted(baseKey, field.getName());
            Object value = get(field, config);

            translationBuilder.add(newBaseKey, label(field, value));

            if (field.getType().isPrimitive()) continue;
            if (field.getType().isEnum()) continue;
            if (field.getType() == String.class) continue;

            generateReflectedTranslations(value, newBaseKey, translationBuilder);
        }
    }

    private static String label(Field field, Object value) {
        if (value instanceof ICoreSettingsContainer container) return container.getCoreSettings().elementName;
        if (SECTION_TITLES.containsKey(field.getName())) return SECTION_TITLES.get(field.getName());
        if (field.getDeclaringClass() == CoreSettings.class && CORE_SETTINGS_LABELS.containsKey(field.getName())) {
            return CORE_SETTINGS_LABELS.get(field.getName());
        }

        return StringUtils.pascalCaseToHumanReadable(field.getName());
    }

    private static Object get(Field field, Object owner) {
        try {
            return field.get(owner);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Cannot read config field " + field, exception);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void addAutomaticTranslations(String[] keys, String packageName, TranslationBuilder translationBuilder) {
        for (String key : keys) {
            translationBuilder.add("%s.%s.%s".formatted(packageName, BrainageHUD.MOD_ID, key), StringUtils.pascalCaseToHumanReadable(key));
        }
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
        // element editor
        translationBuilder.add(
                "text.autoconfig.%s.title".formatted(BrainageHUD.MOD_ID),
                "BrainageHUD Config Editor"
        );

        // keybinds
        addAutomaticTranslations(
                new String[]{
                        "openConfig",
                        "createWaypoint",
                        "manageWaypoints",
                        "inventoryStats"
                },
                "key",
                translationBuilder
        );
        translationBuilder.add("key.category.%s.keys".formatted(BrainageHUD.MOD_ID), BrainageHUD.MOD_NAME);

        // config
        generateReflectedTranslations(new ModConfig(), autoConfigPrefix, translationBuilder);

        // tooltips
        String decimalPlaces = "The number of decimal places displayed.";
        String slotCounts = "When the items are spread over several slots, also list the count in each slot (hotbar left to right, then the inventory rows, then the off hand).";
        String[][] tooltips = {
                {"qualityOfLifeConfig.fullbright", "Overrides the dimension's ambient light. Set to 0 to disable."},
                {"enchantInfoConfig.highlightMaxLevelEnchants", "Makes enchantments at their maximum level bold in item tooltips."},
                {"waypointConfig.showInWorld", "Draws the current dimension's visible waypoints in the world."},
                {"waypointConfig.showWorldCentre", "A built-in waypoint at 0, 63, 0 in every dimension."},
                {"waypointConfig.showBeams", "A beam through the whole height of the world, visible from far away."},
                {"waypointConfig.showMarkers", "A floating, spinning gem above the waypoint, with a pulse on the ground below it when near."},
                {"waypointConfig.showLabels", "The name and distance above the waypoint, readable through walls."},
                {"waypointConfig.alwaysShowNames", "Shows every waypoint's name. Otherwise only the waypoint you look towards and waypoints nearby show their names; the rest show just their distance."},
                {"waypointConfig.labelScalePercent", "In percent."},
                {"dateTimeHudConfig.showDate", "Shows your computer's date."},
                {"dateTimeHudConfig.twelveHourFormat", "Switches between 12 and 24 hour format."},
                {"dateTimeHudConfig.showTimezone", "Shows your computer's timezone."},
                {"entityHudConfig.showCreatures", "Show non-hostile land mobs (animals) in the list of loaded entities."},
                {"entityHudConfig.showWaterCreatures", "Fish, squid, dolphins, axolotls and other water mobs."},
                {"entityHudConfig.showAmbient", "Bats."},
                {"entityHudConfig.showMonsters", "Show hostile mobs (monsters) in the list of loaded entities."},
                {"entityHudConfig.showOthers", "Everything that is not a mob: players, items, projectiles, armour stands, vehicles and so on."},
                {"foodHudConfig.showSlotCounts", slotCounts},
                {"motionHudConfig.decimalPlaces", decimalPlaces},
                {"motionHudConfig.showBlocksPerTick", "Also show the speed in blocks per tick."},
                {"networkHudConfig.colourValues", "Colour ping and TPS from dark green (good) to dark red (bad)."},
                {"networkHudConfig.tpsIntervalsTracked", "How many of the server's game time reports (sent about once a second) the TPS is averaged over."},
                {"performanceHudConfig.showGpuUsage", "The share of each frame's time that the GPU spends drawing it."},
                {"performanceHudConfig.showGpuFrameTime", "How long the GPU takes to draw each frame, in milliseconds."},
                {"positionHudConfig.positionDecimalPlaces", decimalPlaces},
                {"positionHudConfig.showChunkPosition", "Show the player's block position within the current 16x16x16 chunk section."},
                {"positionHudConfig.cCounter", "Shows how many of the 16x16x16 chunk sections around the player are rendered, out of all of them, as on the F3 debug screen."},
                {"positionHudConfig.eCounter", "Shows how many entities are rendered, out of all loaded ones, as on the F3 debug screen."},
                {"positionHudConfig.showDirection", "Show the direction that the player is facing."},
                {"positionHudConfig.showTrueYaw", "When your yaw has wound past ±180°, also show the raw value the game stores."},
                {"positionHudConfig.rotationOnlyWithFarmingTool", "Only show the rotation numbers while you hold an axe, a hoe or a Hypixel SkyBlock farming tool."},
                {"positionHudConfig.rotationDecimalPlaces", decimalPlaces},
                {"positionHudConfig.showLight", "Show sky and block light at the player's feet."},
                {"positionHudConfig.showBiome", "Show the biome at the player's position."},
                {"projectileHudConfig.showSlotCounts", slotCounts},
                {"reachHudConfig.decimalPlaces", decimalPlaces},
                {"reachHudConfig.updateOnAttackClick", "Refresh only when attack changes from released to pressed."},
                {"statusEffectHudConfig.showDurations", "Also show how long each effect has left."},
                {"statusEffectHudConfig.showIcons", "Draw each effect's icon next to its line."},
                {"toggleSprintHudConfig.showInternalValues", "Also show the game's toggle setting and the key's state for sprint and sneak, for debugging."},
                {"armourInfoHudConfig.showDurabilityBar", "Display the durability bar of the item."},
                {"armourInfoHudConfig.durabilityFormat", "Switch between displaying the durability as a percentage, fraction or number (fraction without the denominator)."},
                {"armourInfoHudConfig.durabilityDecimalPlaces", decimalPlaces},
                {"enchantInfoHudConfig.showEnchantments", "Lists the enchantments already on the item."},
                {"enchantInfoHudConfig.showMaxLevels", "Shows the maximum level after enchantments below it."},
                {"enchantInfoHudConfig.showMissingEnchantments", "Lists the enchantments the item could still get."},
                {"enchantInfoHudConfig.showMissingHeader", "Shows a \"Missing:\" line above the missing enchantments."},
        };
        for (String[] tooltip : tooltips) {
            translationBuilder.add("%s.%s.@Tooltip".formatted(autoConfigPrefix, tooltip[0]), tooltip[1]);
        }
    }
}
