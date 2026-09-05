package com.example.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.config;
import static com.example.Utils.computeCheatConfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

//    @Inject(at = @At(value = "RETURN"), method = "applyDamage")
//    private void onApplyDamage(
//            ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
//        // this doesn't run on the client
//        throw new RuntimeException("Intentional crash");
//    }

    @Inject(at = @At(value = "RETURN"), method = "attack")
    private void onAttack(Entity target, CallbackInfo ci) {
        // cheats start
        // TODO -> check if I was sprinting originally
        if (config.isCheatsEnabled &&
                computeCheatConfig().isEthylene &&
                target instanceof Player) {
            this.setSprinting(true);
        }
        // TODO -> remove
//        if (target instanceof PlayerEntity playerEntity && playerEntity.getStatusEffect(StatusEffects.SPEED) instanceof StatusEffectInstance foo)  {
//            MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal(foo.getDuration() + ""), false);
//        }
        // cheats end
    }

    // TODO -> this is fickle, but every solution seems like it's gonna be fickle
    // requiring this to be signed off on when updating would be nice
    @ModifyConstant(method = "causeExtraKnockback", constant = @Constant(doubleValue = 0.6))
    double onCauseExtraKnockbackConstant(double value, Entity entity) {
        // TODO -> check if ethylene is enabled and if I'm taking bad knockback, and return 1.0 for this if I'm not to ethylene harder
        // also, this being raised would technically slow down my ethylene if I don't address this
        if (config.isCheatsEnabled && entity instanceof Player) {
            return computeCheatConfig().attackVelocityBypass;
        }
        return value;
    }
}
