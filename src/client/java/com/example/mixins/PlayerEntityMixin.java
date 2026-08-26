package com.example.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.UntitledClient.cheatConfig;
import static com.example.Utils.handlePvpDamage;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "RETURN"), method = "applyDamage")
    private void onApplyDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (source.getAttacker() instanceof PlayerEntity)
            handlePvpDamage();
    }

    @Inject(at = @At(value = "RETURN"), method = "attack")
    private void onAttack(Entity target, CallbackInfo ci) {
        // cheats start
        // TODO -> check if I was sprinting originally
        if (cheatConfig.isEthylene) {
            this.setSprinting(true);
        }
        // cheats end
    }

    // TODO -> this is fickle, but every solution seems like it's gonna be fickle
    // requiring this to be signed off on when updating would be nice
    @ModifyConstant(method = "attack", constant = @Constant(doubleValue = 0.6))
    double onAttackConstant(double value) {
        // TODO -> check if ethylene is enabled and if I'm taking bad knockback, and return 1.0 for this if I'm not to ethylene harder
        // also, this being raised would technically slow down my ethylene if I don't address this
        return cheatConfig.attackVelocityBypass;
    }
}
