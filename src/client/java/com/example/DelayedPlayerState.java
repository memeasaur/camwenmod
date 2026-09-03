package com.example;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

import net.minecraft.client.player.LocalPlayer;

public class DelayedPlayerState {
    public static final float BASE_FLY_SPEED;
    static {
        if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)
            BASE_FLY_SPEED = player.getAbilities().getFlyingSpeed();
        else
            throw new RuntimeException("pvputils: couldn't getFlySpeed");
    }
}
