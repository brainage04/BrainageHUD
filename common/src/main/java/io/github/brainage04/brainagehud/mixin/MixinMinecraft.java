package io.github.brainage04.brainagehud.mixin;

import com.mojang.blaze3d.systems.TimerQuery;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the timer query behind the F3 GPU utilization: its average GPU time per frame, which the
 * game divides by the frame time to get the utilization.
 */
@Mixin(Minecraft.class)
public interface MixinMinecraft {
    @Accessor("timerQuery")
    TimerQuery brainagehud$getTimerQuery();
}
