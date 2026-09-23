package com.example.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
// codex start
// codex (old code) import net.minecraft.client.renderer.state.level.CameraRenderState;
// codex end
// codex start
import net.minecraft.client.CameraType;
// codex end
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// codex start
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
// codex end
import static com.example.UntitledClient.config;
// codex start
import static com.example.UntitledClient.headRunCameraOffset;
import static com.example.UntitledClient.HEAD_RUN_OFFSET_TYPE;
// codex end

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private boolean isRenderingHandBobbing;

    // codex start
    // codex (old code) @Inject(method = "renderItemInHand", at = @At("HEAD"))
    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void beginHandBobbing(CallbackInfo ci) {
        if (headRunCameraOffset != HEAD_RUN_OFFSET_TYPE.NONE) {
            ci.cancel();
            return;
        }
        isRenderingHandBobbing = true;
    }
    // codex end

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
        // codex start
        // codex (old code) if (config.isViewBobbingCameraShakeDisabled && !isRenderingHandBobbing) {
        if (config.isViewBobbingCameraShakeDisabled &&
                !isRenderingHandBobbing &&
                MINECRAFT_CLIENT_INSTANCE.options.getCameraType() == CameraType.FIRST_PERSON) {
            ci.cancel();
        }
        // codex end
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
