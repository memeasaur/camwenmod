package com.example.mixins;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Constants.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.computeCheatConfig;
import static com.example.Utils.onPvpDamage;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;


@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Shadow
    @Final
    public GameRenderer gameRenderer;

//    @Shadow
//    @Nullable
//    public Entity cameraEntity;

    @Shadow
    public abstract DeltaTracker getDeltaTracker();

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    public abstract @org.jspecify.annotations.Nullable Entity getCameraEntity();

    @Inject(at = @At(value = "HEAD"), method = "startAttack")
    private void onDoAttackHead(CallbackInfoReturnable<Boolean> cir) {
        int previousAttackCooldown = isDebugModeEnabled
                ? MINECRAFT_CLIENT_INSTANCE.missTime
                : 0;
        MINECRAFT_CLIENT_INSTANCE.missTime = 0;
        if (player == null) {
            return;
        }
        if (isDebugModeEnabled) {
            player.sendSystemMessage(Component.literal("miss penalty: " + previousAttackCooldown + " -> " + MINECRAFT_CLIENT_INSTANCE.missTime));
        }
        if (MINECRAFT_CLIENT_INSTANCE.hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
            if (entityHitResult.getEntity() instanceof Player) {
                onPvpDamage();
            }
        }
    }

    @Inject(at = @At(value = "RETURN"), method = "startAttack")
    private void onDoAttackReturn(CallbackInfoReturnable<Boolean> cir) {
//        isAttackCooldown = true;

        if (computeCheatConfig().isSneakyReachEnabled &&
                this.hitResult != null &&
                this.hitResult.getType() == HitResult.Type.MISS &&
                this.getCameraEntity() instanceof Entity camera &&
                player != null) {
//            TODO; // give reach to compensate for the angle and re-check, then attack
            float tickDelta = this.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            var foo = ((ClientPlayerEntityInvoker) this.player).invokePick(
                    camera,
                    4.f,
                    4.f, // TODO ?
                    tickDelta);
            float pitch = camera.getXRot();
            camera.setXRot(0);
            var bar = ((ClientPlayerEntityInvoker) this.player).invokePick(
                    camera,
                    player.blockInteractionRange(),
                    player.entityInteractionRange(),
                    tickDelta);
            camera.setXRot(pitch); // TODO -> debug by not setting this back
            assert gameMode != null;
            if (foo.getType() == HitResult.Type.ENTITY &&
                    bar.getType() == HitResult.Type.ENTITY &&
                    ((EntityHitResult) foo).getEntity() == ((EntityHitResult) bar).getEntity() &&
                    gameMode != null) {
                gameMode.attack(player, ((EntityHitResult) foo).getEntity());
                // TODO -> debugMode this
//                player.sendMessage(Text.literal("cheating"), false);
            }
        }
    }
}
