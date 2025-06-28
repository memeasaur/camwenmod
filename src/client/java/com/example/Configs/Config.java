package com.example.Configs;

import static com.example.Configs.Utils.init;
import static com.example.Configs.Utils.handleSave;

public class Config {
    // TODO -> serialize keybinds in server (?)
    public static boolean isToggleSneakGuiEnabled = false;
    public static boolean isSneakEnabled = false;
    public static boolean isSprintEnabled = false;
    public static boolean isFullbrightEnabled = false;
    public static boolean isFlyBoostEnabled = false;
    public static String currentPotionEnchantmentGlintType = "";
    public static boolean isAttackLoweringDisabled = false;
    public static boolean isSharpnessParticleReverted = true;
    public static boolean isCritParticleReverted = false;
    public static boolean isKnockbackParticleEnabled = false;
    public static boolean isSweepParticleEnabled = false;
    public static boolean isBleedParticleEnabled = false;
    public static boolean isAttackCooldownNotificationEnabled = false;
    public static boolean isAttackCooldownWarningEnabled = false;
    public static boolean isSweepAttackWarningEnabled = false;
    public static boolean isAttackIndicatorDataEnabled = false;

    public static boolean isMovementTogglePvpDisabling = false;
    public static boolean isMovementToggleMirrorPressDisabling = false;
    public static boolean isMovementToggleIndividualPressDisabling = false; // TODO impl

    static {
        init("config", Config.class);
    }
    public static void saveConfig() {
        handleSave("config", Config.class);
    }
}
