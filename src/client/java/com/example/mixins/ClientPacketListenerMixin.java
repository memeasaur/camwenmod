package com.example.mixins;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.config;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Inject(method = "handlePlayerInfoUpdate", at = @At("HEAD"))
    private void onHandlePlayerInfoUpdate(
            ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        if (!config.isPlayerLoginMessagingEnabled) {
            return;
        }
        if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)) {
            return;
        }

        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            if (entry.profile() == null) {
                continue;
            }
            if (!(MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)) {
                return;
            }

            String name = entry.profile().name();
            player.sendSystemMessage(Component.literal(name + " joined"));
        }
    }
}
