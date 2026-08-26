package com.example.mixins;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("findCrosshairTarget")
    HitResult findCrosshairTarget(
            Entity camera,
            double blockInteractionRange,
            double entityInteractionRange,
            float tickDelta);
}
