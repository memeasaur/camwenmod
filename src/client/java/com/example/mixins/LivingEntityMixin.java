package com.example.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import static com.example.UntitledClient.*;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(at = @At(value = "HEAD"), method = "hasStatusEffect", cancellable = true)
    private void onHasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == StatusEffects.NIGHT_VISION && (config.isFullbrightEnabled || FULLBRIGHT_HOLD.isPressed())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
