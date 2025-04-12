package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModPacketEvents;
import io.github.brainage04.brainagehud.util.TimerUtils;
import net.minecraft.client.network.PingMeasurer;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(PingMeasurer.class)
public class MixinPingMeasurer {
    @Inject(method = "onPingResult", at = @At("HEAD"))
    private void onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        if (ModPacketEvents.pingList.size() >= getConfig().networkHudConfig.pingIntervalsTracked) ModPacketEvents.pingList.removeFirst();
        ModPacketEvents.pingList.add(Util.getMeasuringTimeMs() - packet.startTime());
        TimerUtils.ping = Math.round(ModPacketEvents.pingList.stream().mapToLong(Long::longValue).average().orElse(0));
    }
}
