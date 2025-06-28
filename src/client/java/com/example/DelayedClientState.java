package com.example;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;

public class DelayedClientState {
    public static final TextRenderer TEXT_RENDERER = MINECRAFT_CLIENT_INSTANCE.textRenderer;
    public static final GameOptions OPTIONS = MINECRAFT_CLIENT_INSTANCE.options;
    public static final KeyBinding
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
        SNEAK_VANILLA = OPTIONS.sneakKey;
        SPRINT_VANILLA = OPTIONS.sprintKey;
        JUMP_VANILLA = OPTIONS.jumpKey;
        FORWARD_VANILLA = OPTIONS.forwardKey;
        LEFT_VANILLA = OPTIONS.leftKey;
        RIGHT_VANILLA = OPTIONS.rightKey;
        BACKWARD_VANILLA = OPTIONS.backKey;

        ATTACK_VANILLA = OPTIONS.attackKey;
        USE_VANILLA = OPTIONS.useKey;
    }
}
