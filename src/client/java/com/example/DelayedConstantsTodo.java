package com.example;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

public class DelayedConstantsTodo {
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
