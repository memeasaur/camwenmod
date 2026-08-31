package com.example.mixins;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "getMixColor", at = @At("HEAD"), cancellable = true)
    void onGetMixColor(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0xFF40FF40);
    }
}
