package com.example.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    // codex start
    @Inject(method = "attack", at = @At("TAIL"))
    private void onAttackPlayer(net.minecraft.world.entity.player.Player player,
                                net.minecraft.world.entity.Entity target,
                                org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        com.example.LastHitTarget.onAttack(player, target);
    }
    // codex end
    @Inject(method = "isDestroying", at = @At("HEAD"), cancellable = true)
    private void onIsBreakingBlock(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
