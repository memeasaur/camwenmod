package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.FLY_BOOST_MULTIPLIER;
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.DelayedClientState.*;
import static com.example.DelayedClientState.BACKWARD_VANILLA;
import static com.example.DelayedClientState.FORWARD_VANILLA;
import static com.example.DelayedClientState.JUMP_VANILLA;
import static com.example.DelayedClientState.LEFT_VANILLA;
import static com.example.DelayedClientState.RIGHT_VANILLA;
import static com.example.DelayedPlayerState.BASE_FLY_SPEED;
import static com.example.UntitledClient.*;
import static com.example.Utils.getIsKeyBindingPressed;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Objects;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    public float portalEffectIntensity;
    @Shadow
    public float oPortalEffectIntensity;

    @Shadow
    public abstract boolean isUsingItem();

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        Screen currentScreen = MINECRAFT_CLIENT_INSTANCE.gui.screen();
        boolean isCurrentHandledScreen = currentScreen instanceof AbstractContainerScreen<?>;
        boolean isMovementValid = currentScreen == null || isCurrentHandledScreen;
        SNEAK_VANILLA.setDown((getIsKeyBindingPressed(SNEAK_VANILLA) && isMovementValid) || config.isSneakEnabled);
        if (!isCurrentHandledScreen) {
            SPRINT_VANILLA.setDown((getIsKeyBindingPressed(SPRINT_VANILLA) && isMovementValid) || config.isSprintEnabled);
            JUMP_VANILLA.setDown((getIsKeyBindingPressed(JUMP_VANILLA) && isMovementValid) || (isJumpEnabled && !this.isUsingItem())); // TODO -> config this
            FORWARD_VANILLA.setDown((getIsKeyBindingPressed(FORWARD_VANILLA) && isMovementValid) || isForwardEnabled);
            LEFT_VANILLA.setDown((getIsKeyBindingPressed(LEFT_VANILLA) && isMovementValid) || isLeftEnabled);
            RIGHT_VANILLA.setDown((getIsKeyBindingPressed(RIGHT_VANILLA) && isMovementValid) || isRightEnabled);
            BACKWARD_VANILLA.setDown((getIsKeyBindingPressed(BACKWARD_VANILLA) && isMovementValid) || isBackwardEnabled);
        }
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player) {
            if (config.isFlyBoostEnabled && player.isCreative()) {
                Abilities abilities = player.getAbilities();
                if (abilities.flying &&
                        (SPRINT_VANILLA.isDown() || SPRINT_TOGGLE.isDown() || SPRINT_ENABLE.isDown())) {
                    abilities.setFlyingSpeed(BASE_FLY_SPEED * FLY_BOOST_MULTIPLIER);
                    {
                        if (SNEAK_VANILLA.isDown() || SNEAK_TOGGLE.isDown() || SNEAK_ENABLE.isDown())
                            player.setOnGroundWithMovement(player.onGround(), player.getKnownMovement().subtract(0, 0.15D * FLY_BOOST_MULTIPLIER, 0));
                        if (JUMP_VANILLA.isDown())
                            player.setOnGroundWithMovement(player.onGround(), player.getKnownMovement().add(0, 0.15D * FLY_BOOST_MULTIPLIER, 0));
                    }
                } else
                    abilities.setFlyingSpeed(BASE_FLY_SPEED);
            } else
                player.getAbilities().setFlyingSpeed(BASE_FLY_SPEED);

            if (getIsKeyBindingPressed(HEAD_RUN_CAMERA_OFFSET_HOLD)) {
                Entity camera = MINECRAFT_CLIENT_INSTANCE.getCameraEntity();
                assert camera != null;
                // TODO -> I think I have to implement my own freelook for this
                ((EntityInvoker)camera).invokeSetRotation(player.getYRot() - 45.0f, camera.getXRot());
            }


            if (config.isAutoCobweb) {
                onAutoCobwebTick(player);
            }
        }
    }

    @Inject(method = "handlePortalTransitionEffect", at = @At("RETURN"))
    void onTickNausea(CallbackInfo ci) {
        if (config.isDarknessDisabled) {
            this.oPortalEffectIntensity = 0.f;
            this.portalEffectIntensity = 0.f;
        }
    }

//    @Inject(method = "updateHealth", at = @At("RETURN"))
//    void onUpdateHealth(float health, CallbackInfo ci) {
//    }

    @Unique
    boolean hasCurrentUseActionPlacedCobweb = false; // TODO ?

    @Unique
    void onAutoCobwebTick(LocalPlayer player) {
        if (!getIsKeyBindingPressed(USE_VANILLA)) {
            hasCurrentUseActionPlacedCobweb = false;
            return;
        }
        if (hasCurrentUseActionPlacedCobweb) {
            return;
        }
        var mainHandStack = player.getMainHandItem();
        if (!mainHandStack.is(Items.COBWEB)) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) MINECRAFT_CLIENT_INSTANCE.hitResult;
        if (blockHitResult == null) {
            return;
        }
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        hasCurrentUseActionPlacedCobweb = true;
        Objects.requireNonNull(MINECRAFT_CLIENT_INSTANCE.gameMode).useItemOn(
                player, InteractionHand.MAIN_HAND, blockHitResult);
//        KeyBindingMixin keyBindingMixin = (KeyBindingMixin) USE_VANILLA;
//        keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
    }

}
