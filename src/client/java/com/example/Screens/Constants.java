package com.example.Screens;

import com.example.Configs.Config;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import static com.example.Constants.*;
import static com.example.DelayedClientState.TEXT_RENDERER;
import static com.example.Screens.Utils.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

public class Constants {
    private static Checkbox getConfigCheckboxWidget(
            String text, boolean isChecked, Consumer<Boolean> consumer, String tooltip) {
        return Checkbox.builder(Component.literal(text), TEXT_RENDERER)
//                .pos(x, y)
                .selected(isChecked)
                .onValueChange((_, is) -> {
                    consumer.accept(is);
                    config.saveConfig();

                    // Cheats start
                    serializeJsonBlocking("cheat-configs", cheatConfigs);
                    // Cheats end
                })
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .build();
    }

    private static Button getConfigButtonWidget(
            String title, Runnable onPress, String tooltip) {
        return Button.builder(Component.literal(title), _ -> {
                    onPress.run();
                    config.saveConfig();
                    // Cheats start
                    serializeJsonBlocking("cheat-configs", cheatConfigs);
                    // Cheats end
                })
//                .position(x, y)
                .tooltip(Tooltip.create(Component.literal(tooltip)))
                .build();
    }

    private static final Screen TARGETING_MARGIN_BYPASS_RECORDER = getDoubleInputScreen(Component.literal("fing"), number -> computeCheatConfig().targetingMarginBypass = number.floatValue());
    private static final Screen ATTACK_VELOCITY_BYPASS_RECORDER = getDoubleInputScreen(Component.literal("fing1"), number -> computeCheatConfig().attackVelocityBypass = number);

    // TODO -> let mod keybinds be changed here, too
    // TODO -> do the other movement toggles here, too
    public static Screen buildConfig() {
        return buildConfigScreen("pvputils config", List.of(
                getConfigCheckboxWidget("togglesneak gui", config.isToggleSneakGuiEnabled, (is) -> config.isToggleSneakGuiEnabled = is, "modified version of the classic hcf togglesneak's gui"),
                getConfigCheckboxWidget("autorun pvp disable", config.isMovementTogglePvpDisabling, (is) -> config.isMovementTogglePvpDisabling = is, "disables movement toggle when taking/dealing player damage"),
                getConfigCheckboxWidget("movement toggle mirror press cancel", config.isMovementToggleMirrorPressDisabling, (is) -> config.isMovementToggleMirrorPressDisabling = is, "disables movement toggle when autorun movement keys are re-pressed"),
                getConfigCheckboxWidget(
                        "damage taken value notification",
                        config.isDamageTakenValueNotificationEnabled,
                        is -> config.isDamageTakenValueNotificationEnabled = is,
                        ""),
                getConfigCheckboxWidget("sneak", config.isSneakEnabled, is -> config.isSneakEnabled = is, "toggles sneak"),
                getConfigCheckboxWidget("sprint", config.isSprintEnabled, is -> config.isSprintEnabled = is, "toggles sprint"),
                getConfigCheckboxWidget("fake night vision", config.isFullbrightEnabled, is -> config.isFullbrightEnabled = is, "gives the same fullbright that night vision gives you"),
                getConfigCheckboxWidget(
                        "weak attack disabled",
                        config.isWeakAttackSoundDisabled,
                        is -> config.isWeakAttackSoundDisabled = is,
                        ""),
                getConfigCheckboxWidget(
                        "keep sprint",
                        computeCheatConfig().isEthylene,
                        is -> computeCheatConfig().isEthylene = is,
                        "shotbow lol"),
                getConfigButtonWidget(
                        "current: " + computeCheatConfig().targetingMarginBypass + ".change targeting margin",
                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(TARGETING_MARGIN_BYPASS_RECORDER),
                        "current: " + computeCheatConfig().targetingMarginBypass + ". opens float recording screen. default mc is 0, pre-1.14 or whatever is .1. anything higher is just safe aura, gl"),
                getConfigCheckboxWidget(
                        "blindness disable",
                        config.isDarknessDisabled,
                        is -> config.isDarknessDisabled = is,
                        "darkness + blindness + nausea"),
                getConfigCheckboxWidget(
                        "player waypoints",
                        config.isPlayerWaypointsEnabled,
                        is -> config.isPlayerWaypointsEnabled = is,
                        ""),
                getConfigCheckboxWidget(
                        "sneaky reach (beware)",
                        computeCheatConfig().isSneakyReachEnabled,
                        is -> computeCheatConfig().isSneakyReachEnabled = is,
                        ""),
                getConfigButtonWidget(
                        "change attack self velocity multiplier",
                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(ATTACK_VELOCITY_BYPASS_RECORDER),
                        "current: " + computeCheatConfig().attackVelocityBypass + ". opens float recording screen. default mc is 0.6. beware of this setting if the mod has been updated and I haven't re-checked it's mixin"),
                getConfigCheckboxWidget(
                        "nameplate iron colored leather swap",
                        config.isNameplateIronLeatherSwapped,
                        is -> config.isNameplateIronLeatherSwapped = is,
                        ""),
                getConfigButtonWidget(
                        "reset ally nameplates",
                        () -> {
                            config.nameplateUuids.values().removeIf(each -> each == Config.NameplateTeam.ALLY);
                            config.saveConfig();
                        },
                        ""),
                getConfigCheckboxWidget(
                        "player login messaging",
                        config.isPlayerLoginMessagingEnabled,
                        is -> config.isPlayerLoginMessagingEnabled = is,
                        ""),
                getConfigCheckboxWidget(
                        "combat cheats",
                        config.isCheatsEnabled,
                        is -> config.isCheatsEnabled = is,
                        ""),
                getConfigButtonWidget(
                        "send potion count chat message",
                        () -> {
                            if (!(MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)) {
                                return;
                            }

                            int potionCount = 0;
                            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                                if (stack.getItem() instanceof PotionItem &&
                                        stack.get(DataComponents.POTION_CONTENTS) instanceof PotionContents potionContents &&
                                        potionContents.is(Potions.STRONG_HEALING)) {
                                    potionCount += stack.getCount();
                                }
                            }
                            player.connection.sendChat("I have " + potionCount + " health potions");
                        },
                        ""),
                getConfigButtonWidget(
                        "nearby allies/enemies: " + calculateNearbyPlayerCountString(),
                        () -> {
                        },
                        ""),
                getConfigButtonWidget(
                        "current player waypoint category: " + config.playerWaypointCategory.name(),
                        () -> {
                            Config.PlayerWaypointCategory[] values = Config.PlayerWaypointCategory.values();
                            config.playerWaypointCategory = values[(config.playerWaypointCategory.ordinal() + 1) % values.length];
                            config.saveConfig();
                        },
                        "")
        ));
    }

    // TODO -> inline?
    private static String calculateNearbyPlayerCountString() {
        int nearbyTeammates = 0;
        List<AbstractClientPlayer> nearbyPlayers = Objects.requireNonNull(MINECRAFT_CLIENT_INSTANCE.level).players();
        for (Player each : nearbyPlayers) {
            if (each == MINECRAFT_CLIENT_INSTANCE.player) {
                nearbyTeammates++;
            }
            else if (config.nameplateUuids.get(each.getUUID()) instanceof Config.NameplateTeam team &&
                    (team == Config.NameplateTeam.ALLY || team == Config.NameplateTeam.FRIENDLY)) {
                nearbyTeammates++;
            }
        }
        return nearbyTeammates + "/" + (nearbyPlayers.size() - nearbyTeammates);
    }
}
