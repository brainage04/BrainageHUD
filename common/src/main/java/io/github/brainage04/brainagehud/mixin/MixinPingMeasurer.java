package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.util.TimerUtils;
import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(PingDebugMonitor.class)
public class MixinPingMeasurer {
    @Inject(method = "onPongReceived", at = @At("HEAD"))
    private void onPingResult(ClientboundPongResponsePacket packet, CallbackInfo ci) {
        if (ModPacketEvents.pingList.size() >= getConfig().networkHudConfig.pingIntervalsTracked) ModPacketEvents.pingList.removeFirst();
        ModPacketEvents.pingList.add(Util.getMillis() - packet.time());
        TimerUtils.ping = Math.round(ModPacketEvents.pingList.stream().mapToLong(Long::longValue).average().orElse(0));
    }
}
