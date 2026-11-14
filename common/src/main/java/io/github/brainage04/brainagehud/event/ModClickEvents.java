package io.github.brainage04.brainagehud.event;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.util.ClickRateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ModClickEvents {
    private static final ClickRateTracker ATTACK = new ClickRateTracker();
    private static final ClickRateTracker USE = new ClickRateTracker();
    private static long attackClicks;

    public static void onKeyClicked(InputConstants.Key key) {
        Options options = Minecraft.getInstance().options;
        long now = System.currentTimeMillis();

        if (options.keyAttack.matches(key)) {
            ATTACK.recordClick(now);
            attackClicks++;
        }
        if (options.keyUse.matches(key)) USE.recordClick(now);
    }

    /** How many times the attack key has been clicked since the game started. */
    public static long getAttackClicks() {
        return attackClicks;
    }

    public static int getAttackClicksPerSecond() {
        return ATTACK.clicksPerSecond(System.currentTimeMillis());
    }

    public static int getUseClicksPerSecond() {
        return USE.clicksPerSecond(System.currentTimeMillis());
    }
}
