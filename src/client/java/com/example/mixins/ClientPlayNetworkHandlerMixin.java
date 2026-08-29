package com.example.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onHealthUpdate", at = @At("RETURN"))
    void onOnHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
        MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal("healthUpdate"), false);
    }

//    @Inject(method = "onEntityDamage", at = @At("RETURN"))
//    void onOnEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
//    }
}
