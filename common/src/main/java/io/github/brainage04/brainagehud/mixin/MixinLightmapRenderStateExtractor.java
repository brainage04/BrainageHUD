package io.github.brainage04.brainagehud.mixin;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright: the lightmap shader starts every light level from {@code ambientColor} and subtracts
 * {@code darknessEffectScale}, so a positive amount lifts the ambient colour towards white (1 lights
 * everything fully) and a negative amount darkens everything (-1 is black).
 */
@Mixin(LightmapRenderStateExtractor.class)
public class MixinLightmapRenderStateExtractor {
    // TAIL is the return after the state is fully extracted, not the early "nothing changed" returns.
    @Inject(method = "extract", at = @At("TAIL"))
    private void brainagehud$applyFullbright(LightmapRenderState state, float partialTicks, CallbackInfo ci) {
        float amount = getConfig().qualityOfLifeConfig.fullbright;
        if (amount > 0) {
            state.ambientColor = new Vector3f(state.ambientColor).lerp(LightmapRenderStateExtractor.WHITE, amount);
        } else if (amount < 0) {
            state.darknessEffectScale -= amount;
        }
    }
}
