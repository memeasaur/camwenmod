package com.example.Configs;

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

    public void saveConfig() {
        serializeJsonBlocking("config", this);
//        handleSave("config", Config.class);
    }
}
