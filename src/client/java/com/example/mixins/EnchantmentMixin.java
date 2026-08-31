package com.example.mixins;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.config;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "slotMatches", at = @At("HEAD"), cancellable = true)
    void onSlotMatches(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (config.isDepthStriderReverted && slot == EquipmentSlot.LEGS) {
            cir.setReturnValue(true);
        }
    }
}
