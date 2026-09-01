package com.example.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
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


@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Shadow
    @Final
    public GameRenderer gameRenderer;

    @Shadow
    @Nullable
    public Entity cameraEntity;

    @Shadow
    public abstract RenderTickCounter getRenderTickCounter();

    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;

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
        if (MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof LivingEntity target) {
            if (entityHitResult.getEntity() instanceof PlayerEntity) {
                onPvpDamage();
            }
        }
    }

    @Inject(at = @At(value = "RETURN"), method = "doAttack")
    private void onDoAttackReturn(CallbackInfoReturnable<Boolean> cir) {
//        isAttackCooldown = true;

        if (computeCheatConfig().isSneakyReachEnabled &&
                this.crosshairTarget != null &&
                this.crosshairTarget.getType() == HitResult.Type.MISS &&
                this.cameraEntity instanceof Entity camera &&
                player != null) {
//            TODO; // give reach to compensate for the angle and re-check, then attack
            float tickDelta = this.getRenderTickCounter().getTickDelta(false);
            var foo = ((GameRendererInvoker) this.gameRenderer).invokeFindCrosshairTarget(
                    camera,
                    4.f,
                    4.f, // TODO ?
                    tickDelta);
            float pitch = camera.getPitch();
            camera.setPitch(0);
            var bar = ((GameRendererInvoker) this.gameRenderer).invokeFindCrosshairTarget(
                    camera,
                    player.getBlockInteractionRange(),
                    player.getEntityInteractionRange(),
                    tickDelta);
            camera.setPitch(pitch); // TODO -> debug by not setting this back
            assert interactionManager != null;
            if (foo.getType() == HitResult.Type.ENTITY &&
                    bar.getType() == HitResult.Type.ENTITY &&
                    ((EntityHitResult) foo).getEntity() == ((EntityHitResult) bar).getEntity() &&
                    interactionManager != null) {
                interactionManager.attackEntity(player, ((EntityHitResult)foo).getEntity());
                // TODO -> debugMode this
//                player.sendMessage(Text.literal("cheating"), false);
            }
        }
    }
}
