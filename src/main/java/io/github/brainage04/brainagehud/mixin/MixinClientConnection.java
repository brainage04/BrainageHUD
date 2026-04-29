package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModPacketEvents;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class MixinClientConnection {
    @Inject(method = "genericsFtw", at = @At(value = "HEAD"))
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        ModPacketEvents.onPacket();
    }
}