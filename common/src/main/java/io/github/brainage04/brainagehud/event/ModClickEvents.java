package io.github.brainage04.brainagehud.event;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.util.ClickRateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class ModClickEvents {
    private static final ClickRateTracker ATTACK = new ClickRateTracker();
    private static final ClickRateTracker USE = new ClickRateTracker();

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
