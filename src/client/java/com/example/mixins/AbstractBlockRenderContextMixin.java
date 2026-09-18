package com.example.mixins;

import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.isPlayerXrayEnabled;

@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    TODO;
    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private void shouldDrawSide(
            Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (isPlayerXrayEnabled) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
