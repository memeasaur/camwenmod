package com.example;

import com.google.gson.Gson;

import java.util.Objects;
import java.util.concurrent.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

public class Constants {
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new Gson();
    public static final Minecraft MINECRAFT_CLIENT_INSTANCE = Objects.requireNonNull(Minecraft.getInstance());
    public static final Font TEXT_RENDERER = Objects.requireNonNull(MINECRAFT_CLIENT_INSTANCE.font);
}