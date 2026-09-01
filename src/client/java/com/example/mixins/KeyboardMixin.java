package com.example.mixins;

import com.example.Configs.Config;
import net.minecraft.client.Keyboard;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

import static com.example.Constants.*;
import static com.example.DelayedClientState.*;
import static com.example.Screens.Constants.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

@Mixin(value = Keyboard.class)
public class KeyboardMixin {
    @Unique
    private static boolean
            isSneakToggleButtonPressed = false,
            isFullbrightToggleButtonPressed = false,
            isMovementToggleMirrorSequencePressed = false;

    @Inject(at = @At(value = "RETURN"), method = "onKey")
    private void onKey(
            long window,
            int key,
            int scancode,
            int action,
            int modifiers,
            CallbackInfo ci) {
        if (config.isMovementToggleMirrorPressDisabling) {
            if (getIsKeyBindingPressed(SNEAK_VANILLA) == config.isSneakEnabled
                    && getIsKeyBindingPressed(SPRINT_VANILLA) == config.isSprintEnabled
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
                config.isSneakEnabled = !config.isSneakEnabled;
            while (SNEAK_TOGGLE.wasPressed()) {
            }
        } else
            isSneakToggleButtonPressed = false;
        if (getIsKeyBindingPressed(SNEAK_ENABLE)) { // TODO these could benefit from the handling above too, but they aren't toggled so w/e
            config.isSneakEnabled = true;
            while (SNEAK_ENABLE.wasPressed()) {
            }
        }
        if (getIsKeyBindingPressed(SNEAK_DISABLE)) {
            config.isSneakEnabled = false;
            while (SNEAK_DISABLE.wasPressed()) {
            }
        }

        while (SPRINT_TOGGLE.wasPressed())
            config.isSprintEnabled = !config.isSprintEnabled;
        while (SPRINT_ENABLE.wasPressed())
            config.isSprintEnabled = true;
        while (SPRINT_DISABLE.wasPressed())
            config.isSprintEnabled = false;

        if (getIsKeyBindingPressed(FULLBRIGHT_TOGGLE)) {
            if (!isFullbrightToggleButtonPressed)
                config.isFullbrightEnabled = !config.isFullbrightEnabled;
            while (FULLBRIGHT_TOGGLE.wasPressed()) {
            }
        } else
            isFullbrightToggleButtonPressed = false;
        if (getIsKeyBindingPressed(FULLBRIGHT_ENABLE)) { // TODO: see -> sneak handling meme
            config.isFullbrightEnabled = true;
            while (FULLBRIGHT_ENABLE.wasPressed()) {
            }
        }
        if (getIsKeyBindingPressed(FULLBRIGHT_DISABLE)) {
            config.isFullbrightEnabled = false;
            while (FULLBRIGHT_DISABLE.wasPressed()) {
            }
        }

        while (FRIENDLY_TOGGLE.wasPressed()) {
            onAbstractNameplateToggle(Config.NameplateTeam.FRIENDLY);
        }
        while (ALLY_TOGGLE.wasPressed()) {
            onAbstractNameplateToggle(Config.NameplateTeam.ALLY);
        }

        while (KEYBIND_CONFIG.wasPressed()) {
            MINECRAFT_CLIENT_INSTANCE.setScreen(buildConfig());
        }

        while (PLAYER_WAYPOINTS_TOGGLE.wasPressed()) {
            config.isPlayerWaypointsEnabled = !config.isPlayerWaypointsEnabled;
        }

        while (BLOCK_XRAY_TOGGLE.wasPressed()) {
            currentXrayType = Objects.equals(currentXrayType, "block") ? "" : "block";
            MINECRAFT_CLIENT_INSTANCE.worldRenderer.reload();
        }
        while (PLAYER_XRAY_TOGGLE.wasPressed()) {
            currentXrayType = Objects.equals(currentXrayType, "player") ? "" : "player";
            MINECRAFT_CLIENT_INSTANCE.worldRenderer.reload();
        }
    }

    @Unique
    private void doMovementToggleEnable() {
        config.isSneakEnabled = getIsKeyBindingPressed(SNEAK_VANILLA);
        config.isSprintEnabled = getIsKeyBindingPressed(SPRINT_VANILLA);

        isJumpEnabled = getIsKeyBindingPressed(JUMP_VANILLA);
        isForwardEnabled = getIsKeyBindingPressed(FORWARD_VANILLA);
        isLeftEnabled = getIsKeyBindingPressed(LEFT_VANILLA);
        isRightEnabled = getIsKeyBindingPressed(RIGHT_VANILLA);
        isBackwardEnabled = getIsKeyBindingPressed(BACKWARD_VANILLA);

        isMovementToggleMirrorSequencePressed = true;
    }

    @Unique
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

    @Unique
    void onAbstractNameplateToggle(Config.NameplateTeam team) {
        if (!(ComputePlayerRaytrace() instanceof PlayerEntity playerEntity)) {
            return;
        }

        UUID playerUuid = playerEntity.getUuid();
        if (config.nameplateUuids.get(playerUuid) == team) {
            config.nameplateUuids.remove(playerUuid);
        } else {
            config.nameplateUuids.put(playerUuid, team);
        }
        config.saveConfig();
    }
}
