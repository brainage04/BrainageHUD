package io.github.brainage04.brainagehud.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.brainagehud.event.ModClickEvents;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class MixinKeyMapping {
    @Inject(method = "click", at = @At("HEAD"))
    private static void recordClick(InputConstants.Key key, CallbackInfo ci) {
        ModClickEvents.onKeyClicked(key);
    }
}
