package com.example.mixins;

// codex start
// codex (old code) import net.minecraft.client.sounds.SoundEngine;
// codex end
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
// codex start
// codex (old code) import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
// codex end

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    // codex start
    // codex (old code) @Inject(method = "play*", at = @At("HEAD"), cancellable = true)
    // codex (old code) void onPlay(SoundInstance instance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
    // codex (old code) //        if (config.isWeakAttackSoundDisabled && instance.getIdentifier().equals(SoundEvents.PLAYER_ATTACK_NODAMAGE.location())) {
//    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
//    void onPlay(SoundInstance instance, CallbackInfo cir) {
//        if (config.isWeakAttackSoundDisabled && instance.getResourceLocation().equals(SoundEvents.PLAYER_ATTACK_NODAMAGE.location())) {
    // codex end
//            cir.cancel();
//        }
//        if (config.isWeakAttackSoundDisabled && instance.getIdentifier().equals(SoundEvents.PLAYER_ATTACK_WEAK.location())) {
//            cir.cancel();
//        }
//        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
//                player.fishHook != null &&
//                player.getMainHandStack().isOf(Items.FISHING_ROD) &&
//                soundInstance.getId().equals(SoundEvents.BLOCK_IRON_DOOR_CLOSE.id())) {
//            isGrappleReady = true;
//        }
//    }
}
