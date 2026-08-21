package com.example.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.config;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Unique private static int lastAttackedTicks;
    @Inject(at = @At(value = "HEAD"), method = "updateHeldItems")
    private void onUpdateHeldItemsHead(CallbackInfo ci) {
        if (config.isAttackLoweringDisabled && MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
            LivingEntityMixinInterface livingEntityMixinInterface = (LivingEntityMixinInterface) player;
            lastAttackedTicks = livingEntityMixinInterface.getLastAttackedTicks();
            livingEntityMixinInterface.setLastAttackedTicks(Integer.MAX_VALUE);
        }
    }
    @Inject(at = @At(value = "RETURN"), method = "updateHeldItems")
    private void onUpdateHeldItemsReturn(CallbackInfo ci) {
        if (config.isAttackLoweringDisabled && MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
            ((LivingEntityMixinInterface)player).setLastAttackedTicks(lastAttackedTicks);
    }
}
