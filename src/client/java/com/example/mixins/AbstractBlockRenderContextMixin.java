package com.example.mixins;

// codex start
// codex (old code) import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
// codex end
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.UntitledClient.isPlayerXrayEnabled;

@Mixin(AbstractBlockRenderContext.class)
public class AbstractBlockRenderContextMixin {
    @Inject(method = "isFaceCulled", at = @At("HEAD"), cancellable = true)
    void onIsFaceCulled(Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (isPlayerXrayEnabled) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
// codex start
// codex (old code) @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
// codex (old code) private void shouldDrawSide(
// codex (old code) Direction facing, CallbackInfoReturnable<Boolean> cir) {
// codex (old code) if (isPlayerXrayEnabled) {
// codex (old code) cir.setReturnValue(false);
// codex (old code) cir.cancel();
// codex (old code) }
// codex (old code) }

// codex end
}
