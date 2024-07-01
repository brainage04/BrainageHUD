package com.github.brainage04.brainagehud.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

public class RenderUtils {
    public static List<int[]> elementCorners = new ArrayList<>(Arrays.asList(
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1},
            new int[]{-1, -1, -1, -1}
    ));

    public static int[] getCornersWithPadding(int x1, int y1, int x2, int y2) {
        return new int[]{
                x1 - getConfig().padding * 2,
                y1 - getConfig().padding * 2,
                x2 + getConfig().padding * 2,
                y2,
        };
    }

    public static boolean mouseInRect(int x1, int y1, int x2, int y2, double mouseX, double mouseY) {
        return (x1 <= mouseX && mouseX <= x2 && y1 <= mouseY && mouseY <= y2);
    }
}
