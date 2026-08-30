package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static com.example.Utils.serializeJsonBlocking;

public class Config {
    // TODO -> serialize configs in server (?)
    public Map<UUID, String> nameplateUuids = Map.of();
    public boolean isToggleSneakGuiEnabled = false;
    public boolean isSneakEnabled = false;
    public boolean isSprintEnabled = false;
    public boolean isFullbrightEnabled = false;
    public boolean isFlyBoostEnabled = false;
    public String currentPotionEnchantmentGlintType = "";
    public boolean isAttackLoweringDisabled = false;
    public boolean isSharpnessParticleReverted = false;
    public boolean isCritParticleReverted = false;
    public boolean isKnockbackParticleEnabled = false;
    public boolean isSweepParticleEnabled = false;
    public boolean isBleedParticleEnabled = false;
    public boolean isAttackCooldownNotificationEnabled = false;
    public boolean isAttackCooldownWarningEnabled = false;
    public boolean isSweepAttackWarningEnabled = false;
    public boolean isAttackIndicatorDataEnabled = false;
    public boolean isWeakAttackSoundDisabled = false;
    public boolean isDamageTakenValueNotificationEnabled = false;

    public boolean isMovementTogglePvpDisabling = false;
    public boolean isMovementToggleMirrorPressDisabling = false;
    public boolean isMovementToggleIndividualPressDisabling = false; // TODO impl

    //    TODO; // put all keybinds in the real keybind thing?
    public boolean isGuiCheatsPvpDisabling = false;
    public record ClickRecording(int[] clicks, MouseMovement[] movements, boolean isSlow) {
    }
    public ArrayList<ClickRecording> recordedClickSequences = new ArrayList<>();
    public float autoclickerJitterStartingMultiplier = 1.f;
    public float autoclickerJitterEndingMultiplier = 1.f;
    public float autoclickerSlowStartingMultiplier = 1.5f;
    public float autoclickerSlowEndingMultiplier = 1.5f;

    public int glfwToggleAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public int glfwEnableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public int glfwDisableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public boolean isAutoclickerShakeEnabled = true;
    public boolean isAutoClickInventoryEnabled = true;

    // TODO -> triple-clicks possible?
    // TODO -> min interval?
    public boolean isRandomDoubleClickEnabled = true;
    public int randomDoubleClickMaxInterval = 0; // TODO

    public int glfwToggleBlockXrayKeybind = GLFW.GLFW_KEY_UNKNOWN; // TODO -> hold/active/disable
    public int glfwTogglePlayerXrayKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public boolean isAutoCobweb = false;
    public boolean isDarknessDisabled = false;

    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;

    public boolean isGrappleGroundCheckEnabled = false;
    public boolean isTeamHitMessagingEnabled = false;

    public void saveConfig() {
        serializeJsonBlocking("config", this);
//        handleSave("config", Config.class);
    }
}
