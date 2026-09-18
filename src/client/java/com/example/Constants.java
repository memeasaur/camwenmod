package com.example;

import com.google.gson.Gson;
import java.util.concurrent.*;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;

public class Constants {
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new Gson();
    public static final Minecraft MINECRAFT_CLIENT_INSTANCE = Minecraft.getInstance();
    public static final Font TEXT_RENDERER = MINECRAFT_CLIENT_INSTANCE.font;
    public static final Options OPTIONS = MINECRAFT_CLIENT_INSTANCE.options;
    public static final KeyMapping
            SNEAK_VANILLA = OPTIONS.keyShift,
            SPRINT_VANILLA = OPTIONS.keySprint,
            JUMP_VANILLA = OPTIONS.keyJump,
            FORWARD_VANILLA = OPTIONS.keyUp,
            LEFT_VANILLA = OPTIONS.keyLeft,
            RIGHT_VANILLA = OPTIONS.keyRight,
            BACKWARD_VANILLA = OPTIONS.keyDown,
            ATTACK_VANILLA = OPTIONS.keyAttack,
            USE_VANILLA = OPTIONS.keyUse;
}