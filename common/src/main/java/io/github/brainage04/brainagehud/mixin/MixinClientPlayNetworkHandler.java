package io.github.brainage04.brainagehud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.event.ModTickEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(ClientPacketListener.class)
public class MixinClientPlayNetworkHandler {
    private static final String ENSURE_RUNNING_ON_SAME_THREAD =
            "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V";

    /** Sends pings while the Network HUD shows ping, not only while the debug network chart is open. */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showNetworkCharts()Z"
            )
    )
    private boolean sendPingForNetworkHud(boolean showNetworkCharts) {
        return showNetworkCharts ||
                (getConfig().networkHudConfig.showPing &&
                        ModTickEvents.getTicks() % getConfig().networkHudConfig.updatePingTickInterval == 0);
    }

    // both handlers first re-queue themselves onto the render thread; injecting after that call
    // runs the hook once, on the render thread
    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = ENSURE_RUNNING_ON_SAME_THREAD, shift = At.Shift.AFTER))
    private void resetNetworkStatistics(ClientboundLoginPacket packet, CallbackInfo ci) {
        ModPacketEvents.onLogin();
    }

    @Inject(method = "handleSetTime", at = @At(value = "INVOKE", target = ENSURE_RUNNING_ON_SAME_THREAD, shift = At.Shift.AFTER))
    private void recordServerGameTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        ModPacketEvents.onServerGameTime(packet.gameTime());
    }
}
