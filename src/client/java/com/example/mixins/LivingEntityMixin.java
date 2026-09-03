package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import static com.example.UntitledClient.*;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(at = @At(value = "HEAD"), method = "hasEffect", cancellable = true)
    private void onHasStatusEffect(
            Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == MobEffects.NIGHT_VISION && (config.isFullbrightEnabled || FULLBRIGHT_HOLD.isDown())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
