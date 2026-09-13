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
public abstract class LivingEntityMixin {
    @Inject(at = @At(value = "HEAD"), method = "hasEffect", cancellable = true)
    private void onHasStatusEffect(
            Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == MobEffects.NIGHT_VISION && (config.isFullbrightEnabled || FULLBRIGHT_HOLD.isDown())) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    // TODO
//    @Inject(at = @At(value = "HEAD"), method = "getAttributeValue", cancellable = true)
//    private void onGetAttributeValue(
//            Holder<Attribute> attribute, CallbackInfoReturnable<Double> cir) {
//        if (attribute.value() != Attributes.WATER_MOVEMENT_EFFICIENCY) {
//            return;
//        }
//        if (!(MINECRAFT_CLIENT_INSTANCE.level instanceof ClientLevel level)) {
//            return;
//        }
//        if (!(MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)) {
//            return;
//        }
//
//        Holder<Enchantment> depthStrider = level.registryAccess()
//                .lookupOrThrow(Registries.ENCHANTMENT)
//                .getOrThrow(Enchantments.DEPTH_STRIDER);
//        int depthStriderLevel = EnchantmentHelper.getEnchantmentLevel(
//                depthStrider,
//                player);
//        var foo = Objects.requireNonNull(depthStrider.value().effects().get(EnchantmentEffectComponents.ATTRIBUTES))
//                .stream()
//                .filter(each -> each.attribute().value() == Attributes.WATER_MOVEMENT_EFFICIENCY)
//                .mapToDouble(effect -> effect.amount().calculate(depthStriderLevel))
//                .findFirst()
//                .orElse(0.0);
//        cir.setReturnValue(foo);
//    }

}
