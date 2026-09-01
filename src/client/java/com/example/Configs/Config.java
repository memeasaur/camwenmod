package com.example.Configs;

import java.util.Map;
import java.util.UUID;

import static com.example.Utils.serializeJsonBlocking;

public class Config {
    public enum nameplateTeam {
        FRIENDLY,
        ALLY,
        ENEMY,
    }
    public Map<UUID, nameplateTeam> nameplateUuids = Map.of();
    public boolean isToggleSneakGuiEnabled = false;
    public boolean isSneakEnabled = false;
    public boolean isSprintEnabled = false;
    public boolean isFullbrightEnabled = false;
    public boolean isFlyBoostEnabled = false;
    public String currentPotionEnchantmentGlintType = "";
//    public boolean isSharpnessParticleReverted = false;
//    public boolean isCritParticleReverted = false;
    public boolean isWeakAttackSoundDisabled = false;
    public boolean isDamageTakenValueNotificationEnabled = false;
    public boolean isDepthStriderReverted = true;
    public boolean isNameplateIronLeatherSwapped = true;

    public boolean isMovementTogglePvpDisabling = false;
    public boolean isMovementToggleMirrorPressDisabling = false;
    public boolean isGuiCheatsPvpDisabling = false;
    public boolean isAutoCobweb = false;
    public boolean isDarknessDisabled = false;
    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;

    public void saveConfig() {
        serializeJsonBlocking("config", this);
    }
}
