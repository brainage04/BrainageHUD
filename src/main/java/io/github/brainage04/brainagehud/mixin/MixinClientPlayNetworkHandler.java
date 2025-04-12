package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModTickEvents;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/DebugHud;shouldShowPacketSizeAndPingCharts()Z"
            )
    )
    private boolean tick(DebugHud instance) {
        return instance.shouldShowPacketSizeAndPingCharts() ||
                (getConfig().networkHudConfig.showPing &&
                        ModTickEvents.getTicks() % getConfig().networkHudConfig.updatePingTickInterval == 0);
    }
}