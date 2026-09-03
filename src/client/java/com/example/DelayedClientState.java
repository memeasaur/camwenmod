package com.example;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;

public class DelayedClientState {
    public static final Font TEXT_RENDERER = MINECRAFT_CLIENT_INSTANCE.font;
    public static final Options OPTIONS = MINECRAFT_CLIENT_INSTANCE.options;
    public static final KeyMapping
            SNEAK_VANILLA,
            SPRINT_VANILLA,
            JUMP_VANILLA,
            FORWARD_VANILLA,
            LEFT_VANILLA,
            RIGHT_VANILLA,
            BACKWARD_VANILLA,
            ATTACK_VANILLA,
            USE_VANILLA;
    static {
        SNEAK_VANILLA = OPTIONS.keyShift;
        SPRINT_VANILLA = OPTIONS.keySprint;
        JUMP_VANILLA = OPTIONS.keyJump;
        FORWARD_VANILLA = OPTIONS.keyUp;
        LEFT_VANILLA = OPTIONS.keyLeft;
        RIGHT_VANILLA = OPTIONS.keyRight;
        BACKWARD_VANILLA = OPTIONS.keyDown;

        ATTACK_VANILLA = OPTIONS.keyAttack;
        USE_VANILLA = OPTIONS.keyUse;
    }
}
