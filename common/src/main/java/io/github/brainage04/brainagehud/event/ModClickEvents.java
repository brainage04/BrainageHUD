package io.github.brainage04.brainagehud.event;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.util.ClickRateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

/**
 * Tracks attack and use clicks as the game registers them, so that the clicks per second shown by
 * the Keystrokes HUD neither depend on the frame rate nor on whether the HUD is rendering.
 */
public class ModClickEvents {
    private static final ClickRateTracker ATTACK = new ClickRateTracker();
    private static final ClickRateTracker USE = new ClickRateTracker();

    /** Called on the render thread whenever a bound key or mouse button is pressed in game. */
    public static void onKeyClicked(InputConstants.Key key) {
        Options options = Minecraft.getInstance().options;
        long now = System.currentTimeMillis();

        if (options.keyAttack.matches(key)) ATTACK.recordClick(now);
        if (options.keyUse.matches(key)) USE.recordClick(now);
    }

    public static int getAttackClicksPerSecond() {
        return ATTACK.clicksPerSecond(System.currentTimeMillis());
    }

    public static int getUseClicksPerSecond() {
        return USE.clicksPerSecond(System.currentTimeMillis());
    }
}
