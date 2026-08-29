package com.example.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.cheatConfig;
import static com.example.UntitledClient.config;
import static com.example.Utils.onPvpDamage;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(final CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cheatConfig.targetingMarginBypass);
    }

    @Inject(method = "clientDamage", at = @At("HEAD"))
    private void onClientDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
                    player == entity) {
                onPvpDamage();

                if (cheatConfig.isTeamHitMessagingEnabled && Objects.equals(config.nameplateUuids.get(attacker.getUuid()), "ally")) {
                    player.sendMessage(Text.literal("ally damaged you: " + attacker.getName()), false);
                }
            }
            if (MINECRAFT_CLIENT_INSTANCE.player == attacker &&
                    entity instanceof PlayerEntity &&
                    cheatConfig.isTeamHitMessagingEnabled &&
                    Objects.equals(config.nameplateUuids.get(entity.getUuid()), "ally")) {
                attacker.sendMessage(Text.literal("you damaged ally: " + attacker.getName()), false);
            }
        }
    }
}
