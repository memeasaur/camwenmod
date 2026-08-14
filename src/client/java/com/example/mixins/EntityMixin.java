package com.example.mixins;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "getPickRadius", at = @At("HEAD"), cancellable = true)
    private void animatium$pickInflation(final CallbackInfoReturnable<Float> cir) {
        TODO;
        if (ServerFeatureManager.isPresent(ServerFeatures.PICK_INFLATION)) {
            cir.setReturnValue(0.1F);
        }
    }
}
