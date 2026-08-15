package com.example.mixins;

import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.example.Configs.Config.isFullbrightEnabled;
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.DelayedClientState.USE_VANILLA;
import static com.example.UntitledClient.FULLBRIGHT_HOLD;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            at = @At(value = "HEAD"),
            method = "getNightVisionStrength",
            cancellable = true)
    private static void onGetNightVisionStrength(
            LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> cir) {
        if (isFullbrightEnabled || FULLBRIGHT_HOLD.isPressed()) {
            cir.setReturnValue(1.0f);
            cir.cancel();
        }
    }

    @Inject(at = @At(value = "RETURN"), method = "updateCrosshairTarget")
    private static void onUpdateCrosshairTarget(float tickDelta, CallbackInfo ci) {
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
                player.getMainHandStack().isOf(Items.COBWEB) &&
                MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof BlockHitResult) {
            KeyBindingMixin keyBindingMixin = (KeyBindingMixin) USE_VANILLA;
            keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
        }
    }
}
