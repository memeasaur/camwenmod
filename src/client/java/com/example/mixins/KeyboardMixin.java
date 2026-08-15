package com.example.mixins;

import com.github.kwhat.jnativehook.GlobalScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.example.Configs.CheatConfig.*;
import static com.example.Configs.Config.*;
import static com.example.Constants.*;
import static com.example.DelayedClientState.*;
import static com.example.Screens.Constants.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

@Mixin(value = Keyboard.class)
public class KeyboardMixin {
    @Unique
    private static boolean
            // Cheats start
            isBlockToggleKeyPressed = false,
            isPlayerToggleKeyPressed = false,
            isAutoclickerToggleKeyPressed = false,
            isAutoclickerEnableKeyPressed = false,
            isAutoclickerDisableKeyPressed = false,
    // Cheats end
    isSneakToggleButtonPressed = false,
            isFullbrightToggleButtonPressed = false,
            isMovementToggleMirrorSequencePressed = false;
    @Unique
    private static final HashSet<Integer> pressedDuplicateKeybindKeys = new HashSet<>();

    @Inject(at = @At(value = "RETURN"), method = "onKey")
    private void onKey(
            long window,
            int key,
            int scancode,
            int action,
            int modifiers,
            CallbackInfo ci) {
        if (isMovementToggleMirrorPressDisabling) {
            if (getIsKeyBindingPressed(SNEAK_VANILLA) == isSneakEnabled
                    && getIsKeyBindingPressed(SPRINT_VANILLA) == isSprintEnabled
                    && getIsKeyBindingPressed(JUMP_VANILLA) == isJumpEnabled
                    && getIsKeyBindingPressed(FORWARD_VANILLA) == isForwardEnabled
                    && getIsKeyBindingPressed(LEFT_VANILLA) == isLeftEnabled
                    && getIsKeyBindingPressed(RIGHT_VANILLA) == isRightEnabled
                    && getIsKeyBindingPressed(BACKWARD_VANILLA) == isBackwardEnabled) {
                if (!isMovementToggleMirrorSequencePressed)
                    doMovementToggleDisable();
            } else
                isMovementToggleMirrorSequencePressed = false;

        }
        while (MOVEMENT_TOGGLE.wasPressed()) { // TODO -> I should probably just use if if possible
            if (isJumpEnabled
                    || isForwardEnabled
                    || isLeftEnabled
                    || isRightEnabled
                    || isBackwardEnabled)
                doMovementToggleDisable();
            else
                doMovementToggleEnable();
        }
        while (MOVEMENT_ENABLE.wasPressed()) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity)
                doMovementToggleEnable();
        }
        while (MOVEMENT_DISABLE.wasPressed())
            doMovementToggleDisable();


        if (getIsKeyBindingPressed(SNEAK_TOGGLE)) {
            if (!isSneakToggleButtonPressed)
                isSneakEnabled = !isSneakEnabled;
            while (SNEAK_TOGGLE.wasPressed()) {
            }
        } else
            isSneakToggleButtonPressed = false;
        if (getIsKeyBindingPressed(SNEAK_ENABLE)) { // TODO these could benefit from the handling above too, but they aren't toggled so w/e
            isSneakEnabled = true;
            while (SNEAK_ENABLE.wasPressed()) {
            }
        }
        if (getIsKeyBindingPressed(SNEAK_DISABLE)) {
            isSneakEnabled = false;
            while (SNEAK_DISABLE.wasPressed()) {
            }
        }

        while (SPRINT_TOGGLE.wasPressed())
            isSprintEnabled = !isSprintEnabled;
        while (SPRINT_ENABLE.wasPressed())
            isSprintEnabled = true;
        while (SPRINT_DISABLE.wasPressed())
            isSprintEnabled = false;

        if (getIsKeyBindingPressed(FULLBRIGHT_TOGGLE)) {
            if (!isFullbrightToggleButtonPressed)
                isFullbrightEnabled = !isFullbrightEnabled;
            while (FULLBRIGHT_TOGGLE.wasPressed()) {
            }
        } else
            isFullbrightToggleButtonPressed = false;
        if (getIsKeyBindingPressed(FULLBRIGHT_ENABLE)) { // TODO: see -> sneak handling meme
            isFullbrightEnabled = true;
            while (FULLBRIGHT_ENABLE.wasPressed()) {
            }
        }
        if (getIsKeyBindingPressed(FULLBRIGHT_DISABLE)) {
            isFullbrightEnabled = false;
            while (FULLBRIGHT_DISABLE.wasPressed()) {
            }
        }

        while (ALLY_TOGGLE.wasPressed())
            if (ComputePlayerRaytrace() instanceof PlayerEntity playerEntity) {
                UUID playerUuid = playerEntity.getUuid();
                if (Objects.equals(nameplateUuids.get(playerUuid), "ally"))
                    removeNameplateUuidEntry(playerUuid);
                else
                    putNameplateUuidEntry(Map.entry(playerUuid, "ally"));
            }
        while (ENEMY_TOGGLE.wasPressed())
            if (ComputePlayerRaytrace() instanceof PlayerEntity playerEntity) {
                UUID playerUuid = playerEntity.getUuid();
                if (Objects.equals(nameplateUuids.get(playerUuid), "enemy"))
                    removeNameplateUuidEntry(playerUuid);
                else
                    putNameplateUuidEntry(Map.entry(playerUuid, "enemy"));
            }
        while (FOCUS_TOGGLE.wasPressed())
            if (ComputePlayerRaytrace() instanceof PlayerEntity playerEntity) {
                UUID playerUuid = playerEntity.getUuid();
                if (Objects.equals(nameplateUuids.get(playerUuid), "focus"))
                    removeNameplateUuidEntry(playerUuid);
                else
                    putNameplateUuidEntry(Map.entry(playerUuid, "focus"));
            }
        while (NAMEPLATE_CYCLE.wasPressed())
            if (ComputePlayerRaytrace() instanceof PlayerEntity playerEntity) {
                UUID playerUuid = playerEntity.getUuid();
                switch (nameplateUuids.getOrDefault(playerUuid, "")) {
                    case "ally" -> putNameplateUuidEntry(Map.entry(playerUuid, "enemy"));
                    case "enemy" -> putNameplateUuidEntry(Map.entry(playerUuid, "focus"));
                    case "focus" -> removeNameplateUuidEntry(playerUuid);
                    default -> putNameplateUuidEntry(Map.entry(playerUuid, "ally"));
                }
            }

        while (KEYBIND_CONFIG.wasPressed()) {
            MINECRAFT_CLIENT_INSTANCE.setScreen(CONFIG);

            // Cheats start
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e); // TODO
                }
                if (getIsKeyBindingPressed(KEYBIND_CONFIG))
                    MinecraftClient.getInstance().execute(() ->
                            MINECRAFT_CLIENT_INSTANCE.setScreen(CHEAT_CONFIG));
            }).start();
            // Cheats end
        }

        for (Map.Entry<Integer, KeyBinding> entry : duplicateKeybinds.entrySet()) {
            int entryKey = entry.getKey();
            if (getIsKeyPressed(entryKey)) {
                if (!pressedDuplicateKeybindKeys.contains(entryKey)) {
                    entry.getValue().setPressed(true);
                    pressedDuplicateKeybindKeys.add(entryKey);
                }
            } else if (pressedDuplicateKeybindKeys.contains(entryKey)) {
                pressedDuplicateKeybindKeys.remove(entryKey);
                KeyBinding keyBinding = entry.getValue();
                keyBinding.setPressed(getIsKeyBindingPressed(keyBinding));
            }
        }

        // Cheats start
        if (MINECRAFT_CLIENT_INSTANCE.currentScreen == null) { // TODO config this (?)
            if (getIsKeyPressed(glfwToggleBlockXrayKeybind)) {
                if (!isBlockToggleKeyPressed) {
                    isBlockToggleKeyPressed = true;
                    currentXrayType = Objects.equals(currentXrayType, "block") ? "" : "block";
                    MINECRAFT_CLIENT_INSTANCE.worldRenderer.reload();
                }
            } else
                isBlockToggleKeyPressed = false;

            if (getIsKeyPressed(glfwTogglePlayerXrayKeybind)) {
                if (!isPlayerToggleKeyPressed) {
                    isPlayerToggleKeyPressed = true;
                    currentXrayType = Objects.equals(currentXrayType, "player") ? "" : "player";
                    MINECRAFT_CLIENT_INSTANCE.worldRenderer.reload();
                }
            } else
                isPlayerToggleKeyPressed = false;
            if (getIsKeyPressed(glfwToggleAutoclickerKeybind)) { // TODO -> method-ize this
                if (!isAutoclickerToggleKeyPressed) {
                    isAutoclickerToggleKeyPressed = true;
                    if (immutableRecordedAutoclickerClicks.length == 0) {
                        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                            player.sendMessage(Text.literal("no recorded autoclicker macro"), true); // TODO -> idk if these are logged somewhere, they probably are !
                        MINECRAFT_CLIENT_INSTANCE.setScreen(CHEAT_CONFIG);
                    } else {
                        if (isAutoclickerEnabled) {
                            GlobalScreen.removeNativeMouseListener(AUTOCLICKER_MOUSE_LISTENER);
                            nullableCurrentHeldAutoclickerTask = null;
                        } else {// TODO -> external gui for this
                            GlobalScreen.addNativeMouseListener(AUTOCLICKER_MOUSE_LISTENER);
                            if (GLFW.glfwGetMouseButton(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS)
                                handleAutoclickerMouseHeldDown();
                        }
                        isAutoclickerEnabled = !isAutoclickerEnabled;
                    }
                }
            } else
                isAutoclickerToggleKeyPressed = false;
            if (getIsKeyPressed(glfwEnableAutoclickerKeybind)) {
                if (!isAutoclickerEnableKeyPressed) {
                    isAutoclickerEnableKeyPressed = true;
                    if (immutableRecordedAutoclickerClicks.length == 0) {
                        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                            player.sendMessage(Text.literal("no recorded autoclicker macro"), true); // TODO -> idk if these are logged somewhere, they probably are !
                        MINECRAFT_CLIENT_INSTANCE.setScreen(CHEAT_CONFIG);
                    } else {
                        if (!isAutoclickerEnabled) {
                            GlobalScreen.addNativeMouseListener(AUTOCLICKER_MOUSE_LISTENER);
                            isAutoclickerEnabled = true;
                            if (GLFW.glfwGetMouseButton(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS)
                                handleAutoclickerMouseHeldDown(); // TODO method-ize this
                        }
                    }
                }
            } else
                isAutoclickerEnableKeyPressed = false;
            if (getIsKeyPressed(glfwDisableAutoclickerKeybind)) {
                if (!isAutoclickerDisableKeyPressed) {
                    isAutoclickerDisableKeyPressed = true;
                    GlobalScreen.removeNativeMouseListener(AUTOCLICKER_MOUSE_LISTENER);
                    isAutoclickerEnabled = false;
                    nullableCurrentHeldAutoclickerTask = null;
                }
            } else
                isAutoclickerDisableKeyPressed = false;
            if (getIsKeyPressed(glfwToggleMirrorMovementKeybind)) {
                if (nullableMirrorMovementPlayer != null) {
                    nullableMirrorMovementPlayer = null;
                } else if (MINECRAFT_CLIENT_INSTANCE.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof PlayerEntity player) {
                    nullableMirrorMovementPlayer = player;
                }
            }
        }
        // Cheats end
    }

    @Unique
    private void doMovementToggleEnable() {
        isSneakEnabled = getIsKeyBindingPressed(SNEAK_VANILLA);
        isSprintEnabled = getIsKeyBindingPressed(SPRINT_VANILLA);

        isJumpEnabled = getIsKeyBindingPressed(JUMP_VANILLA);
        isForwardEnabled = getIsKeyBindingPressed(FORWARD_VANILLA);
        isLeftEnabled = getIsKeyBindingPressed(LEFT_VANILLA);
        isRightEnabled = getIsKeyBindingPressed(RIGHT_VANILLA);
        isBackwardEnabled = getIsKeyBindingPressed(BACKWARD_VANILLA);

        isMovementToggleMirrorSequencePressed = true;
    }

    private PlayerEntity ComputePlayerRaytrace() {
        final double REACH = 50.f;
        float tickDelta = MINECRAFT_CLIENT_INSTANCE.getRenderTickCounter().getTickDelta(true);

        ClientPlayerEntity player = MINECRAFT_CLIENT_INSTANCE.player;
        Vec3d cameraPos = player.getCameraPosVec(tickDelta);
        Vec3d rotationVec = player.getRotationVec(tickDelta);
        Vec3d endPos = cameraPos.add(rotationVec.multiply(REACH));

        Box searchBox = player.getBoundingBox()
                .stretch(rotationVec.multiply(REACH))
                .expand(.3D);

        EntityHitResult hitResult = ProjectileUtil.raycast(
                player,
                cameraPos,
                endPos,
                searchBox,
                entity -> !entity.isSpectator()
                        && entity.canHit()
                        && entity instanceof PlayerEntity,
                REACH * REACH
        );

        if (hitResult != null && hitResult.getEntity() instanceof PlayerEntity targetPlayer) {
            return targetPlayer;
        }

        return null;
    }
}
