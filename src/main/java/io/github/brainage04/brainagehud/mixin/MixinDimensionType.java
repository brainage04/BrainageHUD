package io.github.brainage04.brainagehud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import net.minecraft.world.level.dimension.DimensionType;

@Mixin(DimensionType.class)
public class MixinDimensionType {
    @Inject(method = "ambientLight", at = @At("HEAD"), cancellable = true)
    private void getLight$injected(CallbackInfoReturnable<Float> cir) {
        if (getConfig().qualityOfLifeConfig.fullbright != 0) {
            cir.setReturnValue(getConfig().qualityOfLifeConfig.fullbright);
        }
    }
}
