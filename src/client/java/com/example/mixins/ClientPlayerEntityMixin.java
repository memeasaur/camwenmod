package com.example.mixins;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Configs.CheatConfig.isAutoCobweb;
import static com.example.Configs.Config.*;
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

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    //    @Inject(method="addEnchantedHitParticles", at = @At("HEAD"), cancellable = true)
//    private void onAddEnchantedHitParticles(Entity target, CallbackInfo ci) {
//        ci.cancel();
//    }TODO remove
    boolean bLastTickBlockHitResult = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        Screen currentScreen = MINECRAFT_CLIENT_INSTANCE.currentScreen;
        boolean isCurrentHandledScreen = currentScreen instanceof HandledScreen<?>;
        boolean isMovementValid = currentScreen == null || isCurrentHandledScreen;
        SNEAK_VANILLA.setPressed((getIsKeyBindingPressed(SNEAK_VANILLA) && isMovementValid) || isSneakEnabled);
        if (!isCurrentHandledScreen) { // you better not fork-remove this! or you are gonna have a bad time!
            SPRINT_VANILLA.setPressed((getIsKeyBindingPressed(SPRINT_VANILLA) && isMovementValid) || isSprintEnabled);
            JUMP_VANILLA.setPressed((getIsKeyBindingPressed(JUMP_VANILLA) && isMovementValid) || isJumpEnabled);
            FORWARD_VANILLA.setPressed((getIsKeyBindingPressed(FORWARD_VANILLA) && isMovementValid) || isForwardEnabled);
            LEFT_VANILLA.setPressed((getIsKeyBindingPressed(LEFT_VANILLA) && isMovementValid) || isLeftEnabled);
            RIGHT_VANILLA.setPressed((getIsKeyBindingPressed(RIGHT_VANILLA) && isMovementValid) || isRightEnabled);
            BACKWARD_VANILLA.setPressed((getIsKeyBindingPressed(BACKWARD_VANILLA) && isMovementValid) || isBackwardEnabled);
        }
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
            if (isFlyBoostEnabled && player.isCreative()) {
                PlayerAbilities abilities = player.getAbilities();
                if (abilities.flying &&
                        (SPRINT_VANILLA.isPressed() || SPRINT_TOGGLE.isPressed() || SPRINT_ENABLE.isPressed())) {
                    abilities.setFlySpeed(BASE_FLY_SPEED * FLY_BOOST_MULTIPLIER);
                    {
                        if (SNEAK_VANILLA.isPressed() || SNEAK_TOGGLE.isPressed() || SNEAK_ENABLE.isPressed())
                            player.setMovement(player.isOnGround(), player.getMovement().subtract(0, 0.15D * FLY_BOOST_MULTIPLIER, 0));
                        if (JUMP_VANILLA.isPressed())
                            player.setMovement(player.isOnGround(), player.getMovement().add(0, 0.15D * FLY_BOOST_MULTIPLIER, 0));
                    }
                } else
                    abilities.setFlySpeed(BASE_FLY_SPEED);
            } else
                player.getAbilities().setFlySpeed(BASE_FLY_SPEED);


            if (isAutoCobweb) {
                onAutoCobwebTick(player);
            }
        }
    }

    @Unique
    boolean hasCurrentUseActionPlacedCobweb = false; // TODO ?
    @Unique
    void onAutoCobwebTick(ClientPlayerEntity player) {
        if (!getIsKeyBindingPressed(USE_VANILLA)) {
            hasCurrentUseActionPlacedCobweb = false;
            return;
        } else {
            player.sendMessage(Text.literal("yes"), false);
        }
        if (hasCurrentUseActionPlacedCobweb) {
            return;
        }
        var mainHandStack = player.getMainHandStack();
        if (!mainHandStack.isOf(Items.COBWEB)) {
            return;
        }
        BlockHitResult blockHitResult = (BlockHitResult) MINECRAFT_CLIENT_INSTANCE.crosshairTarget;
        if (blockHitResult == null) {
            return;
        }
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return;
        }

        hasCurrentUseActionPlacedCobweb = true;
        player.sendMessage(Text.literal("place"), false);
        KeyBindingMixin keyBindingMixin = (KeyBindingMixin) USE_VANILLA;
        keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
    }
}
