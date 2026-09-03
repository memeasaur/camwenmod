package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.config;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "matchingSlot", at = @At("HEAD"), cancellable = true)
    void onSlotMatches(EquipmentSlot slot, CallbackInfoReturnable<Boolean> cir) {
        if (true && slot == EquipmentSlot.LEGS) { // TODO config.isDepthStriderReverted
            cir.setReturnValue(true);
        }
    }
}
