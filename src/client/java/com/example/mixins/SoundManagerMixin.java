package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.config;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play*", at = @At("HEAD"), cancellable = true)
    void onPlay(SoundInstance soundInstance, CallbackInfo ci) {
        if (config.isWeakAttackSoundDisabled && soundInstance.getIdentifier().equals(SoundEvents.PLAYER_ATTACK_NODAMAGE.location())) {
            ci.cancel();
        }
//        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
//                player.fishHook != null &&
//                player.getMainHandStack().isOf(Items.FISHING_ROD) &&
//                soundInstance.getId().equals(SoundEvents.BLOCK_IRON_DOOR_CLOSE.id())) {
//            isGrappleReady = true;
//        }
    }
}
