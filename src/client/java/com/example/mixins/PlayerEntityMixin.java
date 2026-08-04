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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Configs.CheatConfig.isEthylene;
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
        if (isEthylene) {
            this.setSprinting(true);
        }
        // cheats end
    }
}
