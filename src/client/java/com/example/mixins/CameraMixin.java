package com.example.mixins;

import com.example.UntitledClient;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.DelayedConstantsTodo.JUMP_VANILLA;
import static com.example.UntitledClient.headRunCameraOffset;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract float yRot();

    @Shadow
    public abstract float xRot();

    @Inject(method = "alignWithEntity", at = @At(value = "RETURN"))
    void onAlignWithEntity(CallbackInfo ci) {
        if (!(this.entity instanceof LocalPlayer player)) {
            return;
        }
        if (headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.NONE) { // TODO ?
            return;
        }
        float yawOffset = getHeadRunYawOffset();
        switch (MINECRAFT_CLIENT_INSTANCE.options.getCameraType()) {
            case FIRST_PERSON -> {
                this.setRotation(
                        player.getYRot() + yawOffset,
                        player.getXRot());
            }
            case THIRD_PERSON_BACK, THIRD_PERSON_FRONT -> {
            }
        }
    }

    // codex start
    @Inject(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;move(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.BEFORE))
    private void offsetThirdPersonOrbit(float partialTick, CallbackInfo ci) {
        if (!(this.entity instanceof LocalPlayer)
                || headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.NONE
                || MINECRAFT_CLIENT_INSTANCE.options.getCameraType().isFirstPerson()) {
            return;
        }

        // At this point vanilla has already mirrored front view, so preserving xRot handles both views.
        this.setRotation(this.yRot() + getHeadRunYawOffset(), this.xRot());
    }

    @Unique
    private static float getHeadRunYawOffset() {
        float jumpModifier = JUMP_VANILLA.isDown()
                ? headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.LEFT ? 12.0F : -12.0F
                : 0.0F;
        return headRunCameraOffset.delta + jumpModifier;
    }
    // codex end
}
