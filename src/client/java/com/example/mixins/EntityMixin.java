package com.example.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

//    @Inject(method = "onDamaged", at = @At("HEAD"))
//    void onOnDamaged(DamageSource damageSource, CallbackInfo ci) {
//        // doesn't run, not sure why it doesn't when the packet works totally fine but w/e
    // these methods are probably both overridden. it is: livingEntity overrides it
//    }

//    @Inject(method = "clientDamage", at = @At("HEAD"))
//    private void onClientDamage(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
//        //  -> this literally never runs?
//    }
}
