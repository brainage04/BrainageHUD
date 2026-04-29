package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModTickEvents;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showNetworkCharts()Z"
            )
    )
    private boolean tick(DebugScreenOverlay instance) {
        return instance.showNetworkCharts() ||
                (getConfig().networkHudConfig.showPing &&
                        ModTickEvents.getTicks() % getConfig().networkHudConfig.updatePingTickInterval == 0);
    }
}