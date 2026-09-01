package com.example.mixins;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.config;
import static com.example.Utils.onPvpDamage;

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

    @Inject(method = "onEntityDamage", at = @At("RETURN"))
    void onOnEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        if (!(MINECRAFT_CLIENT_INSTANCE.world instanceof ClientWorld clientWorld)) {
            return;
        }

        Entity entity = clientWorld.getEntityById(packet.entityId());
        if (clientWorld.getEntityById(packet.sourceCauseId()) instanceof PlayerEntity attacker) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player &&
                    player == entity) {
                // TODO -> player == attacker?
                onPvpDamage();

//                if (config.isTeamHitMessagingEnabled && Objects.equals(config.nameplateUuids.get(attacker.getUuid()), "ally")) {
//                    player.sendMessage(Text.literal("ally damaged you: " + attacker.getName().getString()), false);
//                }
            }
//            if (MINECRAFT_CLIENT_INSTANCE.player == attacker &&
//                    entity instanceof PlayerEntity &&
//                    config.isTeamHitMessagingEnabled &&
//                    Objects.equals(config.nameplateUuids.get(entity.getUuid()), "ally")) {
//                attacker.sendMessage(Text.literal("you damaged ally: " + attacker.getName().getString()), false);
//            }
        }
    }

    @Inject(method = "onEntitySpawn", at = @At("RETURN"))
    void onOnEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        // TODO -> waypoint this?
        if (packet.getEntityType() == EntityType.LIGHTNING_BOLT &&
                MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
            player.sendMessage(Text.literal(packet.getX() + ", " + packet.getY() + ", " + packet.getZ()), false);
        }
    }
}
