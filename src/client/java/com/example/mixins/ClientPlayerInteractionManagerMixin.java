package com.example.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "isDestroying", at = @At("HEAD"), cancellable = true)
    private void onIsBreakingBlock(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
