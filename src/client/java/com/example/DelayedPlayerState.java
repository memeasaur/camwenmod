package com.example;

import net.minecraft.client.network.ClientPlayerEntity;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

public class DelayedPlayerState {
    public static final float BASE_FLY_SPEED;
    static {
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
            BASE_FLY_SPEED = player.getAbilities().getFlySpeed();
        else
            throw new RuntimeException("pvputils: couldn't getFlySpeed");
    }
}
