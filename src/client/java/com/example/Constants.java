package com.example;

import com.google.gson.Gson;
import java.util.concurrent.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextColor;

public class Constants {
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new Gson();
    public static final Minecraft MINECRAFT_CLIENT_INSTANCE = Minecraft.getInstance();
    public static final byte FLY_BOOST_MULTIPLIER = 4;
}