package io.github.brainage04.brainagehud.mixin;

import io.github.brainage04.brainagehud.hud.PerformanceHud;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(DebugScreenEntryList.class)
public class MixinDebugScreenEntryList {
    @Inject(method = "isCurrentlyEnabled", at = @At("RETURN"), cancellable = true)
    private void enableGpuProfilerForPerformanceHud(Identifier entry, CallbackInfoReturnable<Boolean> cir) {
        if (entry.equals(DebugScreenEntries.GPU_UTILIZATION) && PerformanceHud.isGpuUsageEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
