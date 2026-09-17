package com.example.mixins;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.isLeftCameraOffsetActive;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At(value = "RETURN"))
    void onAlignWithEntity(CallbackInfo ci) {
        if (isLeftCameraOffsetActive && this.entity instanceof LocalPlayer player) {
            //            TODO; // handle jumping
            float leftCameraOffset = -45.0f;
            float rightCameraOffset = 45.0f;
            if (player.input.keyPresses.left()) {
                this.setRotation(player.getYRot() + leftCameraOffset, player.getXRot());
            }
            if (player.input.keyPresses.right()) {
                this.setRotation(player.getYRot() + rightCameraOffset, player.getXRot());
            }
        }
    }
}
