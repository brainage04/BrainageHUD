package io.github.brainage04.brainagehud.keys;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.config.hud.custom.enchant_info.EnchantInfoHudConfig;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.util.ChatFeedback;
import io.github.brainage04.brainagehud.util.ConfigUtils;
import io.github.brainage04.brainagehud.util.InventoryCounter;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/**
 * BrainageHUD's keys, listed under its own category: Create Waypoint and Manage Waypoints (B and U
 * by default, like Xaero's Minimap), Toggle Enchant Info Compact (K) and Inventory Stats (unbound
 * by default).
 */
public final class ModKeys {
    private ModKeys() {}

    public static void initialize() {
        KeyMapping.Category category = HudRendererLib.getKeyCategory(BrainageHUD.MOD_ID);
        KeyMapping createWaypoint = key("createWaypoint", InputConstants.KEY_B, category);
        KeyMapping manageWaypoints = key("manageWaypoints", InputConstants.KEY_U, category);
        KeyMapping toggleEnchantInfoCompact =
                key("toggleEnchantInfoCompact", InputConstants.KEY_K, category);
        KeyMapping inventoryStats =
                key("inventoryStats", InputConstants.UNKNOWN.getValue(), category);

        HudRendererLib.platform()
                .registerEndClientTick(
                        minecraft -> {
                            while (createWaypoint.consumeClick()) {
                                WaypointActions.createAtPlayer(null);
                            }
                            while (manageWaypoints.consumeClick()) {
                                minecraft.setScreenAndShow(new WaypointsScreen(null));
                            }
                            while (toggleEnchantInfoCompact.consumeClick()) {
                                toggleEnchantInfoCompact();
                            }
                            while (inventoryStats.consumeClick()) {
                                printInventoryStats(minecraft);
                            }
                        });
    }

    /** Switches the Enchant Info HUD between compact and full lines, and says which one is on. */
    private static void toggleEnchantInfoCompact() {
        EnchantInfoHudConfig config = ConfigUtils.getConfig().enchantInfoHudConfig;
        config.compactMode = !config.compactMode;
        ConfigUtils.saveConfig();

        ChatFeedback.success(
                "Enchant Info compact mode %s.".formatted(config.compactMode ? "on" : "off"));
    }

    private static void printInventoryStats(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        List<ItemStack> slots = InventoryCounter.slots(player);
        if (slots.stream().allMatch(ItemStack::isEmpty)) {
            ChatFeedback.warning("Your inventory is empty.");
            return;
        }

        ChatFeedback.info("Inventory stats:");
        for (int slot = 0; slot < slots.size(); slot++) {
            ItemStack stack = slots.get(slot);
            if (stack.isEmpty()) continue;
            String where =
                    switch (slot) {
                        case 36 -> "Feet";
                        case 37 -> "Legs";
                        case 38 -> "Chest";
                        case 39 -> "Head";
                        case 40 -> "Off hand";
                        default -> "Slot " + slot;
                    };
            String line =
                    "%s: %d × %s"
                            .formatted(
                                    where,
                                    stack.getCount(),
                                    BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (!stack.getComponentsPatch().isEmpty()) line += " " + stack.getComponentsPatch();
            ChatFeedback.detail(line);
        }
    }

    private static KeyMapping key(String name, int keycode, KeyMapping.Category category) {
        KeyMapping key =
                new KeyMapping(
                        "key.%s.%s".formatted(BrainageHUD.MOD_ID, name),
                        InputConstants.Type.KEYBOARD,
                        keycode,
                        category);
        HudRendererLib.platform().registerKeyMapping(key);
        return key;
    }
}
