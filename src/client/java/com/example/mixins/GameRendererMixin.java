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
}
