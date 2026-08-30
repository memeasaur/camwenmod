package com.example.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.config;

@Mixin(targets = "net/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier")
public interface StatusEffectFogModifierMixin {
    @Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
    default void onShouldApply(
            LivingEntity entity, float tickDelta, CallbackInfoReturnable<Boolean> cir) {
        if (config.isDarknessDisabled) {
            cir.setReturnValue(false);
        }
    }
}
