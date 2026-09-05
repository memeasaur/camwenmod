package com.example.Configs;

import java.util.HashMap;
import java.util.UUID;
import net.minecraft.network.chat.TextColor;

import static com.example.Utils.serializeJsonBlocking;

public class Config {
    public enum NameplateTeam {
        FRIENDLY(TextColor.GREEN),
        ALLY(TextColor.AQUA);
        //        ENEMY,
//        FOCUS,
        public final TextColor color;

        NameplateTeam(TextColor color) {
            this.color = color;
        }
    }

    public HashMap<UUID, NameplateTeam> nameplateUuids = new HashMap<>();
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
    public boolean isDepthStriderReverted = true; // TODO ?
    public boolean isNameplateIronLeatherSwapped = true;

    public boolean isMovementTogglePvpDisabling = false;
    public boolean isMovementToggleMirrorPressDisabling = false;
    public boolean isGuiCheatsPvpDisabling = false;
    public boolean isAutoCobweb = false;
    public boolean isDarknessDisabled = false;
    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;
    public boolean isPlayerLoginMessagingEnabled = false;
    TODO;
    public boolean isCheatsEnabled = true;

    public void saveConfig() {
        serializeJsonBlocking("config", this);
    }
}
