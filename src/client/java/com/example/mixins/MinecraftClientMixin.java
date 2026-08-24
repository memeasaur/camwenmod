package com.example.mixins;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Constants.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.handlePvpDamage;


@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(at = @At(value = "HEAD"), method = "doAttack")
    private void onDoAttackHead(CallbackInfoReturnable<Boolean> cir) {
        int previousAttackCooldown = isDebugModeEnabled
                ? MINECRAFT_CLIENT_INSTANCE.attackCooldown
                : 0;
        MINECRAFT_CLIENT_INSTANCE.attackCooldown = 0;
        if (player == null) {
            return;
        }
        if (isDebugModeEnabled) {
            player.sendMessage(
                    Text.literal("miss penalty: " + previousAttackCooldown + " -> " + MINECRAFT_CLIENT_INSTANCE.attackCooldown),
                    false);
        }

        float attackCooldown = player.getAttackCooldownProgress(0f);
        boolean isWeakAttack = player.getAttackCooldownProgress(.5f) < KNOCKBACK_ATTACK_STRENGTH;
        lastHitStrength = attackCooldown < 1f
                ? Text.literal(String.format("%.2f", attackCooldown))
                .setStyle(isWeakAttack
                        ? RED_TEXT_STYLE
                        : GREEN_TEXT_STYLE)
                : EMPTY_TEXT;
        if (isWeakAttack) {
            if (config.isAttackCooldownWarningEnabled) {
                player.playSound(SoundEvents.BLOCK_ANVIL_LAND, .25f, .25f); // TODO this should just replace the weak attack sound (?) -> the sounds for miss etc. should just be entirely configurable
            }
        }
        lastHitDisplayTimer = 0;
        if (MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
            lastHitDistance = String.format("%.2f", player.getEyePos().distanceTo(entityHitResult.getPos()));

            if (!isWeakAttack) {
                if (isSprintReset && player.isSprinting()) {
                    if (config.isKnockbackParticleEnabled)
                        MINECRAFT_CLIENT_INSTANCE.particleManager.addEmitter(target, new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.COBWEB.getDefaultState()));
                    // TODO ParticleTypes.FLASH, ParticleTypes.FIREWORK, ParticleTypes.ELECTRIC_SPARK, ParticleTypes.COMPOSTER, ParticleTypes.GLOW, ParticleTypes.HAPPY_VILLAGER, ParticleTypes.HEART, ParticleTypes.SCRAPE, ParticleTypes.WAX_OFF, ParticleTypes.WAX_ON, or just list all particles in the options dropdown, that's better
                    isSprintReset = false;
                } else if (!isYLower  // player.fallDistance <= 0.0F
                        || player.isOnGround()
                        || player.isClimbing()
                        || player.isTouchingWater()
                        || player.hasStatusEffect(StatusEffects.BLINDNESS)
                        || player.hasVehicle()
//                                && target instanceof LivingEntity
//                            && !player.isSprinting()
                ) { // this is pulled from: playerEntity.java -> attack()
                    if (config.isSweepParticleEnabled)
                        MINECRAFT_CLIENT_INSTANCE.particleManager.addEmitter(target, new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.BEACON.getDefaultState()));
                    if (config.isSweepAttackWarningEnabled)
                        player.playSound(SoundEvents.BLOCK_ANVIL_LAND, .25f, .25f);
                    // TODO -> selector for all sounds to pick for this
                }
            }

            if (config.isBleedParticleEnabled) {
                MINECRAFT_CLIENT_INSTANCE.particleManager.addEmitter(target, new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_WIRE.getDefaultState().with(Properties.POWER, 1))); // TODO Blocks.RED_CANDLE.getDefaultState()
            }

            if (entityHitResult.getEntity() instanceof PlayerEntity) {
                handlePvpDamage();
            }

            if (config.isSharpnessParticleReverted && player.getMainHandStack().getEnchantments().getEnchantmentEntries().stream().anyMatch(x -> Enchantments.SHARPNESS.equals(x.getKey().getKey().orElse(null)))) // TODO fuck
                player.addEnchantedHitParticles(target);
            if (config.isCritParticleReverted && player.getMovement().getY() < 0.0f && !player.isOnGround() && !player.isClimbing() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.BLINDNESS) && !player.hasVehicle()) // TODO -> p.isSprinting() config option + player.fallDistance > 0.0F?
                player.addCritParticles(target);
        } else
            lastHitDistance = "";
    }

    @Inject(at = @At(value = "RETURN"), method = "doAttack")
    private void onDoAttackReturn(CallbackInfoReturnable<Boolean> cir) {
        isAttackCooldown = true;

        if (this.crosshairTarget != null &&
                this.crosshairTarget.getType() == HitResult.Type.MISS) {
//            TODO; // give reach to compensate for the angle and re-check, then attack
        }
    }
//    @Inject(method = "doItemUse", at = @At(value = "HEAD"))
//    private void onDoItemUse(CallbackInfo ci) {
//        MINECRAFT_CLIENT_INSTANCE.interactionManager.isBreakingBlock()
//    } TODO REMOVE
}
