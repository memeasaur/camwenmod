package com.example.mixins;

import com.example.Configs.Config;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static com.example.Constants.*;
import static com.example.DelayedClientState.*;
import static com.example.Screens.Constants.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

@Mixin(value = KeyboardHandler.class)
public class KeyboardMixin {
    @Unique
    private static boolean
            isSneakToggleButtonPressed = false,
            isFullbrightToggleButtonPressed = false,
            isMovementToggleMirrorSequencePressed = false;

    @Inject(at = @At(value = "RETURN"), method = "keyPress")
    private void onKeyPress(
            long handle, int action, KeyEvent event, CallbackInfo ci) {
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
        while (MOVEMENT_TOGGLE.consumeClick()) { // TODO -> I should probably just use if if possible
            if (isJumpEnabled
                    || isForwardEnabled
                    || isLeftEnabled
                    || isRightEnabled
                    || isBackwardEnabled)
                doMovementToggleDisable();
            else
                doMovementToggleEnable();
        }
        while (MOVEMENT_ENABLE.consumeClick()) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer)
                doMovementToggleEnable();
        }
        while (MOVEMENT_DISABLE.consumeClick())
            doMovementToggleDisable();


        if (getIsKeyBindingPressed(SNEAK_TOGGLE)) {
            if (!isSneakToggleButtonPressed)
                config.isSneakEnabled = !config.isSneakEnabled;
            while (SNEAK_TOGGLE.consumeClick()) {
            }
        } else
            isSneakToggleButtonPressed = false;
        if (getIsKeyBindingPressed(SNEAK_ENABLE)) { // TODO these could benefit from the handling above too, but they aren't toggled so w/e
            config.isSneakEnabled = true;
            while (SNEAK_ENABLE.consumeClick()) {
            }
        }
        if (getIsKeyBindingPressed(SNEAK_DISABLE)) {
            config.isSneakEnabled = false;
            while (SNEAK_DISABLE.consumeClick()) {
            }
        }

        while (SPRINT_TOGGLE.consumeClick())
            config.isSprintEnabled = !config.isSprintEnabled;
        while (SPRINT_ENABLE.consumeClick())
            config.isSprintEnabled = true;
        while (SPRINT_DISABLE.consumeClick())
            config.isSprintEnabled = false;

        if (getIsKeyBindingPressed(FULLBRIGHT_TOGGLE)) {
            if (!isFullbrightToggleButtonPressed)
                config.isFullbrightEnabled = !config.isFullbrightEnabled;
            while (FULLBRIGHT_TOGGLE.consumeClick()) {
            }
        } else
            isFullbrightToggleButtonPressed = false;
        if (getIsKeyBindingPressed(FULLBRIGHT_ENABLE)) { // TODO: see -> sneak handling meme
            config.isFullbrightEnabled = true;
            while (FULLBRIGHT_ENABLE.consumeClick()) {
            }
        }
        if (getIsKeyBindingPressed(FULLBRIGHT_DISABLE)) {
            config.isFullbrightEnabled = false;
            while (FULLBRIGHT_DISABLE.consumeClick()) {
            }
        }

        while (FRIENDLY_TOGGLE.consumeClick()) {
            onAbstractNameplateToggle(Config.NameplateTeam.FRIENDLY);
        }
        while (ALLY_TOGGLE.consumeClick()) {
            onAbstractNameplateToggle(Config.NameplateTeam.ALLY);
        }

        while (KEYBIND_CONFIG.consumeClick()) {
            MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(buildConfig());
        }

        while (PLAYER_WAYPOINTS_TOGGLE.consumeClick()) {
            config.isPlayerWaypointsEnabled = !config.isPlayerWaypointsEnabled;
        }

        while (BLOCK_XRAY_TOGGLE.consumeClick()) {
            currentXrayType = Objects.equals(currentXrayType, "block") ? "" : "block";
            MINECRAFT_CLIENT_INSTANCE.levelRenderer.resetLevelRenderData();
        }
        while (PLAYER_XRAY_TOGGLE.consumeClick()) {
            currentXrayType = Objects.equals(currentXrayType, "player") ? "" : "player";
            MINECRAFT_CLIENT_INSTANCE.levelRenderer.resetLevelRenderData();
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
    private Player ComputePlayerRaytrace() {
        final double REACH = 50.f;
        float tickDelta = MINECRAFT_CLIENT_INSTANCE.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        LocalPlayer player = Objects.requireNonNull(MINECRAFT_CLIENT_INSTANCE.player);
        Vec3 cameraPos = player.getEyePosition(tickDelta);
        Vec3 rotationVec = player.getViewVector(tickDelta);
        Vec3 endPos = cameraPos.add(rotationVec.scale(REACH));

        AABB searchBox = player.getBoundingBox()
                .expandTowards(rotationVec.scale(REACH))
                .inflate(.3D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player,
                cameraPos,
                endPos,
                searchBox,
                entity -> !entity.isSpectator()
                        && entity.isPickable()
                        && entity instanceof Player,
                REACH * REACH
        );

        if (hitResult != null && hitResult.getEntity() instanceof Player targetPlayer) {
            return targetPlayer;
        }

        return null;
    }

    @Unique
    void onAbstractNameplateToggle(Config.NameplateTeam team) {
        if (!(ComputePlayerRaytrace() instanceof Player playerEntity)) {
            return;
        }

        UUID playerUuid = playerEntity.getUUID();
        if (config.nameplateUuids.get(playerUuid) == team) {
            config.nameplateUuids.remove(playerUuid);
        } else {
            config.nameplateUuids.put(playerUuid, team);
        }
        config.saveConfig();
    }
}
