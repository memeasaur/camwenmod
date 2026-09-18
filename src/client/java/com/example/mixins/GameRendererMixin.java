package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.joml.Quaternionfc;

import static com.example.UntitledClient.config;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Redirect(
            method = "bobView",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    private void disableViewBobbingCameraShake(PoseStack poseStack, Quaternionfc rotation) {
        if (!config.isViewBobbingCameraShakeDisabled) {
            poseStack.mulPose(rotation);
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
