package com.example.mixins;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Configs.Config.isWeakAttackSoundDisabled;

@Mixin(SoundManager.class)
public class SoundManagerMixin {
    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    void onPlay(SoundInstance soundInstance, CallbackInfo ci) {
        if (isWeakAttackSoundDisabled && soundInstance.getId().equals(SoundEvents.ENTITY_PLAYER_ATTACK_WEAK.id())) {
            ci.cancel();
        }
    }
}
