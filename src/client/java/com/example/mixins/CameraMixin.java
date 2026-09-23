package com.example.mixins;

import com.example.UntitledClient;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
// codex start
import net.minecraft.world.level.BlockGetter;
// codex end
import net.minecraft.world.entity.Entity;
// codex start
// codex (old code) import org.jspecify.annotations.Nullable;
import org.jetbrains.annotations.Nullable;
// codex end
// codex start
import net.minecraft.world.phys.Vec3;
// codex end
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
// codex start
import org.spongepowered.asm.mixin.Unique;
// codex end
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// codex start
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
// codex end
import static com.example.DelayedConstantsTodo.JUMP_VANILLA;
import static com.example.UntitledClient.headRunCameraOffset;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private @Nullable Entity entity;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    // codex start
    // codex (old code) @Inject(method = "alignWithEntity", at = @At(value = "RETURN"))
    @Inject(method = "setup", at = @At(value = "RETURN"))
    // codex end
    void onAlignWithEntity(CallbackInfo ci) {
        if (!(this.entity instanceof LocalPlayer player)) {
            return;
        }
        if (headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.NONE) { // TODO ?
            return;
        }
        // codex start
        // codex (old code) float modifier = JUMP_VANILLA.isDown() ? headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.LEFT ? 12.f : -12.f : 0.f;
        float yawOffset = getHeadRunYawOffset();
        if (MINECRAFT_CLIENT_INSTANCE.options.getCameraType().isFirstPerson()) {
            this.setRotation(player.getYRot() + yawOffset, player.getXRot());
        }
        // codex end
    }

    // codex start
    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    // codex end
    @Shadow
    private Vec3 position;

    @Shadow
    protected abstract void setPosition(Vec3 position);
    // codex start
    @Inject(method = "setup", at = @At("TAIL"))
    private void offsetThirdPersonOrbit(
            BlockGetter level,
            Entity focusedEntity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci) {
        if (!(this.entity instanceof LocalPlayer)
                || headRunCameraOffset == UntitledClient.HEAD_RUN_OFFSET_TYPE.NONE
                || MINECRAFT_CLIENT_INSTANCE.options.getCameraType().isFirstPerson()) {
            return;
        }

        float yawOffset = getHeadRunYawOffset();
        // Vanilla has completed either orbit here, including the mirrored front-camera transform.
        this.setRotation(this.yRot + yawOffset, this.xRot);
        Vec3 eyePosition = ((LocalPlayer) this.entity).getEyePosition(partialTick);
        Vec3 rotatedOrbit = this.position.subtract(eyePosition)
                .yRot((float) Math.toRadians(-yawOffset));
        this.setPosition(eyePosition.add(rotatedOrbit));
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
