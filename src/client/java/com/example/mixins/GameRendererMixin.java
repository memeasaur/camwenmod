package com.example.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
// codex start
// codex (old code) import net.minecraft.client.renderer.state.level.CameraRenderState;
// codex end
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.config;

import net.minecraft.client.renderer.GameRenderer;

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

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(
            // codex start
            // codex (old code) CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
            PoseStack poseStack, float partialTick, CallbackInfo ci) {
            // codex end
        if (config.isViewBobbingCameraShakeDisabled && !isRenderingHandBobbing) {
            ci.cancel();
        }
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
