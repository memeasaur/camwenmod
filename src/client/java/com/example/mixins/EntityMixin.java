package com.example.mixins;

import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.cheatConfigs;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getTargetingMargin", at = @At("HEAD"), cancellable = true)
    private void onGetTargetingMargin(final CallbackInfoReturnable<Float> cir) {
        if (MINECRAFT_CLIENT_INSTANCE.getCurrentServerEntry() instanceof ServerInfo serverInfo) {
            cir.setReturnValue(cheatConfigs.get(serverInfo.address).targetingMarginBypass);
        }
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
