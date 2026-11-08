package io.github.brainage04.brainagehud.mixin;

import com.mojang.blaze3d.systems.TimerQuery;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Minecraft.class)
public interface MixinMinecraft {
    @Accessor("timerQuery")
    TimerQuery brainagehud$getTimerQuery();
}
