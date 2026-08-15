package com.example.mixins;

import com.example.Constants;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
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
         TODO // -> this just polls btw, nice job mc, which would make this obviously an autoclicker
        // it also doesn't have access to fall distance apparently
        // I think I might as well have it just hold right click, since an organic click would never actually beat this
        if (MINECRAFT_CLIENT_INSTANCE.player != null)
            MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.of(String.valueOf(Constants.MINECRAFT_CLIENT_INSTANCE.player.fallDistance)), false);
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
                player.getMainHandStack().isOf(Items.COBWEB) &&
                player.fallDistance > 10.f &&
                MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof BlockHitResult) {
            KeyBindingMixin keyBindingMixin = (KeyBindingMixin) USE_VANILLA;
            keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
        }
    }
}
