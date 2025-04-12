package io.github.brainage04.brainagehud.mixin;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @Redirect(
            method = "update(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;[F)V"
            )
    )
    private void redirectNightVisionFactor(RenderPass renderPass, String uniformName, float[] floats) {
        if (getConfig().qualityOfLifeConfig.fullbright && uniformName.equals("NightVisionFactor")) {
            // Override with our custom Night Vision strength
            renderPass.setUniform(uniformName, 1.0F); // Full Night Vision effect
        } else {
            // Use the original value if config is disabled
            renderPass.setUniform(uniformName, floats);
        }
    }
}
