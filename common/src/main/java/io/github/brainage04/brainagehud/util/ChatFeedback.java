package io.github.brainage04.brainagehud.util;

import io.github.brainage04.brainagehud.BrainageHUD;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatFeedback {
    private static final Component PREFIX =
            Component.literal("[%s] ".formatted(BrainageHUD.MOD_NAME))
                    .withStyle(ChatFormatting.GRAY);

    private ChatFeedback() {}

    public static void info(Component message) {
        send(Component.empty().append(PREFIX).append(body(ChatFormatting.WHITE, message)));
    }

    public static void info(String message) {
        info(Component.literal(message));
    }

    public static void success(Component message) {
        send(Component.empty().append(PREFIX).append(body(ChatFormatting.GREEN, message)));
    }

    public static void success(String message) {
        success(Component.literal(message));
    }

    public static void warning(Component message) {
        send(Component.empty().append(PREFIX).append(body(ChatFormatting.YELLOW, message)));
    }

    public static void warning(String message) {
        warning(Component.literal(message));
    }

    public static void error(Component message) {
        send(Component.empty().append(PREFIX).append(body(ChatFormatting.RED, message)));
    }

    public static void error(String message) {
        error(Component.literal(message));
    }

    public static void detail(Component message) {
        send(body(ChatFormatting.WHITE, message));
    }

    public static void detail(String message) {
        detail(Component.literal(message));
    }

    private static Component body(ChatFormatting colour, Component message) {
        return Component.empty().withStyle(colour).append(message);
    }

    private static void send(Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) minecraft.player.sendSystemMessage(message);
    }
}
