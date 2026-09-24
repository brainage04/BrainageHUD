package io.github.brainage04.brainagehud.keys;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.BrainageHUD;
import io.github.brainage04.brainagehud.screen.WaypointsScreen;
import io.github.brainage04.brainagehud.util.InventoryCounter;
import io.github.brainage04.brainagehud.waypoint.WaypointActions;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static io.github.brainage04.brainagehud.command.core.ModCommands.feedback;

/**
 * BrainageHUD's keys, listed under its own category: Create Waypoint and Manage Waypoints (B and U
 * by default, like Xaero's Minimap) and Inventory Stats (unbound by default).
 */
public final class ModKeys {
    private ModKeys() {}

    public static void initialize() {
        KeyMapping.Category category = HudRendererLib.getKeyCategory(BrainageHUD.MOD_ID);
        KeyMapping createWaypoint = key("createWaypoint", GLFW.GLFW_KEY_B, category);
        KeyMapping manageWaypoints = key("manageWaypoints", GLFW.GLFW_KEY_U, category);
        KeyMapping inventoryStats = key("inventoryStats", InputConstants.UNKNOWN.getValue(), category);

        HudRendererLib.platform().registerEndClientTick(minecraft -> {
            while (createWaypoint.consumeClick()) {
                WaypointActions.createAtPlayer(null);
            }
            while (manageWaypoints.consumeClick()) {
                minecraft.setScreenAndShow(new WaypointsScreen(null));
            }
            while (inventoryStats.consumeClick()) {
                printInventoryStats(minecraft);
            }
        });
    }

    /**
     * Lists every occupied slot in chat with its item, count and data components that differ
     * from the item's defaults (its damage, enchantments, custom name and so on).
     */
    private static void printInventoryStats(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        List<ItemStack> slots = InventoryCounter.slots(player);
        for (int slot = 0; slot < slots.size(); slot++) {
            ItemStack stack = slots.get(slot);
            if (stack.isEmpty()) continue;
            String where = slot == slots.size() - 1 ? "Off hand" : "Slot " + slot;
            String line = "%s: %d × %s".formatted(where, stack.getCount(), BuiltInRegistries.ITEM.getKey(stack.getItem()));
            if (!stack.getComponentsPatch().isEmpty()) line += " " + stack.getComponentsPatch();
            feedback(Component.literal(line));
        }
    }

    private static KeyMapping key(String name, int keycode, KeyMapping.Category category) {
        KeyMapping key = new KeyMapping("key.%s.%s".formatted(BrainageHUD.MOD_ID, name), InputConstants.Type.KEYSYM, keycode, category);
        HudRendererLib.platform().registerKeyMapping(key);
        return key;
    }
}
