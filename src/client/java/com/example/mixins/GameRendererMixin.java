package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.FULLBRIGHT_HOLD;
import static com.example.UntitledClient.config;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            at = @At(value = "HEAD"),
            method = "getNightVisionScale",
            cancellable = true)
    private static void onGetNightVisionStrength(
            LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (config.isFullbrightEnabled || FULLBRIGHT_HOLD.isDown()) {
            cir.setReturnValue(1.0f);
            cir.cancel();
        }
    }
}
