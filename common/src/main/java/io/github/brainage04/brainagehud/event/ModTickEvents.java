package io.github.brainage04.brainagehud.event;


public class ModTickEvents {
    private static int ticks = 0;

    public static int getTicks() {
        return ticks;
    }

    public static void onClientTick() {
        ticks++;
    }
}
