package com.example.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.config;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onHealthUpdate", at = @At("HEAD"))
    void onOnHealthUpdate(HealthUpdateS2CPacket packet, CallbackInfo ci) {
        // TODO -> get amount of damage taken from teammate? might be impossible
        if (!(MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)) {
            return;
        }
        float previous = player.getHealth();
        float health = packet.getHealth();
        if (previous == health) {
            return;
        }

        if (config.isDamageTakenValueNotificationEnabled && previous > health) {
            player.sendMessage(Text.literal(String.valueOf(previous - health)), false);
        }
    }

//    @Inject(method = "onEntityDamage", at = @At("RETURN"))
//    void onOnEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
//    }
}
