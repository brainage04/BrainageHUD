package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.event.ModPacketEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class MixinClientConnection {
    @Inject(method = "handlePacket", at = @At(value = "HEAD"))
    private static void handlePacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        ModPacketEvents.onPacket();
    }
}