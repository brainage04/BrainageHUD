package com.github.brainage04.brainagehud.util;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class RenderUtils {
    // todo: fix this init with static block that checks number of hud elements (using reflection is probably the best bet)
    public static List<int[]> elementCorners = new ArrayList<>(Arrays.asList(
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1}
    ));

    // could be related to that alignment bug in the readme?
    public static int[] getCornersWithPadding(int x1, int y1, int x2, int y2) {
        return new int[]{
                x1 - getConfig().elementPadding * 2,
                y1 - getConfig().elementPadding * 2,
                x2 + getConfig().elementPadding * 2,
                y2
        };
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
