package com.example.mixins;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("findCrosshairTarget")
    HitResult invokeFindCrosshairTarget(
            Entity camera,
            double blockInteractionRange,
            double entityInteractionRange,
            float tickDelta);
}
