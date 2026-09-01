package com.example.Configs;

import org.lwjgl.glfw.GLFW;

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
//    public boolean isAttackLoweringDisabled = false;
//    public boolean isSharpnessParticleReverted = false;
//    public boolean isCritParticleReverted = false;
//    public boolean isKnockbackParticleEnabled = false;
//    public boolean isSweepParticleEnabled = false;
//    public boolean isBleedParticleEnabled = false;
//    public boolean isAttackCooldownNotificationEnabled = false;
//    public boolean isAttackCooldownWarningEnabled = false;
//    public boolean isSweepAttackWarningEnabled = false;
//    public boolean isAttackIndicatorDataEnabled = false;
    public boolean isWeakAttackSoundDisabled = false;
    public boolean isDamageTakenValueNotificationEnabled = false;
    public boolean isDepthStriderReverted = true;
    public boolean isNameplateIronLeatherSwapped = true;

    public boolean isMovementTogglePvpDisabling = false;
    public boolean isMovementToggleMirrorPressDisabling = false;

    //    TODO; // put all keybinds in the real keybind thing?
    public boolean isGuiCheatsPvpDisabling = false;
    public int glfwToggleBlockXrayKeybind = GLFW.GLFW_KEY_UNKNOWN; // TODO -> hold/active/disable
    public int glfwTogglePlayerXrayKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public boolean isAutoCobweb = false;
    public boolean isDarknessDisabled = false;

    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;

    public void saveConfig() {
        serializeJsonBlocking("config", this);
    }
}
