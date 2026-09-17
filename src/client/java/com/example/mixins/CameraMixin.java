package com.example.mixins;

import com.example.UntitledClient;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.headRunCameraOffset;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "alignWithEntity", at = @At(value = "RETURN"))
    void onAlignWithEntity(CallbackInfo ci) {
        if (!(this.entity instanceof LocalPlayer player)) {
            return;
        }
        if (headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.NONE) { // TODO ?
            return;
        }
        this.setRotation(player.getYRot() + headRunCameraOffset.delta, player.getXRot());
    }
}
