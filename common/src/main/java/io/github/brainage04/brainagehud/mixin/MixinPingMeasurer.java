package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModPacketEvents;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingDebugMonitor.class)
public class MixinPingMeasurer {
    @Inject(method = "onPongReceived", at = @At("HEAD"))
    private void onPingResult(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        ModPacketEvents.onPong(Util.getMillis() - packet.time());
    }
}
