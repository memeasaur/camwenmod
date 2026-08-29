package com.example.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.cheatConfig;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(final CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cheatConfig.targetingMarginBypass);
    }

    @Inject(method = "clientDamage", at = @At("HEAD"))
    private void onClientDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        TODO;
    }
}
