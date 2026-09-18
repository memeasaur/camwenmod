package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.config;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private boolean isRenderingHandBobbing;

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void beginHandBobbing(CallbackInfo ci) {
        isRenderingHandBobbing = true;
    }

    @Inject(method = "renderItemInHand", at = @At("TAIL"))
    private void endHandBobbing(CallbackInfo ci) {
        isRenderingHandBobbing = false;
    }

    @ModifyVariable(method = "bobView", at = @At("STORE"), index = 3)
    private float disableCameraBobbingShake(float value) {
        if (config.isViewBobbingCameraShakeDisabled && !isRenderingHandBobbing) {
            return 0.0F;
        }
        return value;
    }

//    @Inject(
//            at = @At(value = "HEAD"),
//            method = "nightVisionScale",
//            cancellable = true)
//    private static void onGetNightVisionStrength(
//            LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
//        if (config.isFullbrightEnabled) {
//            cir.setReturnValue(1.0f);
//            cir.cancel();
//        }
//    }
}
