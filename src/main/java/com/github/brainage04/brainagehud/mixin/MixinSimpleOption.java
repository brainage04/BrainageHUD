package com.github.brainage04.brainagehud.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Source:
// https://github.com/Sjouwer/gamma-utils/blob/1.21/src/main/java/io/github/sjouwer/gammautils/mixin/MixinSimpleOption.java
@Mixin(SimpleOption.class)
public class MixinSimpleOption<T> {
    @Shadow
    @Final
    Text text;

    @SuppressWarnings("unused")
    @Shadow
    T value;

    /**
     * Mixin to allow saving "invalid" gamma values into the options file
     */
    @Inject(method = "getCodec", at = @At("HEAD"), cancellable = true)
    private void returnFakeCodec(CallbackInfoReturnable<Codec<Double>> info) {
        if (text.getString().equals(I18n.translate("options.gamma"))) {
            info.setReturnValue(Codec.DOUBLE);
        }
    }

    /**
     * Mixin to allow setting "invalid" gamma values
     */
    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void setRealValue(T value, CallbackInfo info) {
        if (text.getString().equals(I18n.translate("options.gamma"))) {
            this.value = value;
            info.cancel();
        }
    }
}
