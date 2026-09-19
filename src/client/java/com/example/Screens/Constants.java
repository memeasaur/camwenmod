package com.example.Screens;

import com.example.Configs.Config;

import java.util.*;
import java.util.function.Consumer;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
// codex start
import net.minecraft.client.gui.components.EditBox;
// codex end
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static com.example.Constants.*;
import static com.example.DelayedConstantsTodo.TEXT_RENDERER;
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

    // codex start
    private static EditBox getConfigFloatInputWidget(
            String label, float value, Consumer<Float> consumer, String tooltip) {
        EditBox input = new EditBox(
                TEXT_RENDERER,
                Button.DEFAULT_WIDTH,
                Button.DEFAULT_HEIGHT,
                Component.literal(label));
        input.setMaxLength(16);
        input.setValue(Float.toString(value));
        input.setHint(Component.literal(label));
        input.setTooltip(Tooltip.create(Component.literal(tooltip)));
        input.setResponder(text -> {
            try {
                float parsed = Float.parseFloat(text);
                if (!Float.isFinite(parsed)) {
                    throw new NumberFormatException("non-finite float");
                }
                input.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
                consumer.accept(parsed);
                config.saveConfig();
                serializeJsonBlocking("cheat-configs", cheatConfigs);
            } catch (NumberFormatException ignored) {
                input.setTextColor(0xFF5555);
            }
        });
        return input;
    }
    // codex end

    // codex start
//    private static Screen getTargetingMarginBypassStaticRecorder() {
//        return getDoubleInputScreen(Component.literal("fing"), number -> computeCheatConfig().staticTargetingMarginBypass = number.floatValue());
//    }
//    private static Screen getTargetingMarginBypassMovingRecorder() {
//        return getDoubleInputScreen(Component.literal("fing=4"), number -> computeCheatConfig().movingTargetMarginBypass = number.floatValue());
//    }
    // codex end
    //    private static final Screen TARGETING_MARGIN_WIDTH_BYPASS_RECORDER = getDoubleInputScreen(Component.literal("fpng"), number -> computeCheatConfig().targetingMarginWidthBypass = number.floatValue());
//    private static final Screen ATTACK_VELOCITY_BYPASS_RECORDER = getDoubleInputScreen(Component.literal("fing1"), number -> computeCheatConfig().attackVelocityBypass = number);
//    private static final Screen COBWEB_BYPASS_DELTA_RECORDER = getDoubleInputScreen(Component.literal("fing2"), number -> computeCheatConfig().cobwebRangeBypassDelta = number);

    // TODO -> let mod keybinds be changed here, too
    // TODO -> do the other movement toggles here, too
    public static Screen buildConfig() {
        return buildConfigScreen("pvputils config", List.of(
                getConfigCheckboxWidget("togglesneak gui", config.isToggleSneakGuiEnabled, (is) -> config.isToggleSneakGuiEnabled = is, "modified version of the classic hcf togglesneak's gui"),
//                getConfigCheckboxWidget("autorun pvp disable", config.isMovementTogglePvpDisabling, (is) -> config.isMovementTogglePvpDisabling = is, "disables movement toggle when taking/dealing player damage"),
                getConfigCheckboxWidget("movement toggle mirror press cancel", config.isMovementToggleMirrorPressDisabling, (is) -> config.isMovementToggleMirrorPressDisabling = is, "disables movement toggle when autorun movement keys are re-pressed"),
                getConfigCheckboxWidget(
                        "ignore backward after sprint reset",
                        config.isBackwardSprintResetSuppressionEnabled,
                        is -> config.isBackwardSprintResetSuppressionEnabled = is,
                        "allows one W+S sprint reset after each hit, then ignores Back while W is held until the next hit"),
                getConfigCheckboxWidget(
                        "disable view bobbing camera shake",
                        config.isViewBobbingCameraShakeDisabled,
                        is -> config.isViewBobbingCameraShakeDisabled = is,
                        "keeps view bobbing enabled while removing only the camera shake"),
                // codex start
                getConfigCheckboxWidget(
                        "mark targeted teammates on external overlay",
                        config.isTeammateTargetCrosshairMarkerEnabled,
                        is -> config.isTeammateTargetCrosshairMarkerEnabled = is,
                        "draws a red X over the crosshair on the external player waypoint overlay when targeting a teammate"),
                // codex end
                getConfigCheckboxWidget(
                        "damage taken value notification",
                        config.isDamageTakenValueNotificationEnabled,
                        is -> config.isDamageTakenValueNotificationEnabled = is,
                        ""),
//                getConfigCheckboxWidget(
//                        "sneak",
//                        config.isSneakEnabled,
//                        is -> config.isSneakEnabled = is,
//                        "toggles sneak"),
//                getConfigCheckboxWidget("sprint", config.isSprintEnabled, is -> config.isSprintEnabled = is, "toggles sprint"),
//                getConfigCheckboxWidget("fake night vision", config.isFullbrightEnabled, is -> config.isFullbrightEnabled = is, "gives the same fullbright that night vision gives you"),
//                getConfigCheckboxWidget(
//                        "weak attack disabled",
//                        config.isWeakAttackSoundDisabled,
//                        is -> config.isWeakAttackSoundDisabled = is,
//                        ""),
//                getConfigCheckboxWidget(
//                        "keep sprint",
//                        computeCheatConfig().isEthylene,
//                        is -> computeCheatConfig().isEthylene = is,
//                        "shotbow lol"),
                getConfigCheckboxWidget(
                        "targeting margin revert",
                        computeCheatConfig().isTargetingMarginReverted,
                        is -> computeCheatConfig().isTargetingMarginReverted = is,
                        "will flag hard on pre-1.12 or whatever it is that made the hitboxes smaller"),
                // codex start
//                getConfigButtonWidget(
//                        "current: " + computeCheatConfig().staticTargetingMarginBypass + ".change targeting margin (static)",
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(getTargetingMarginBypassStaticRecorder()),
//                        "current: " + computeCheatConfig().staticTargetingMarginBypass + ". opens float recording screen. safe aura, gl"),
//                getConfigButtonWidget(
//                        "current: " + computeCheatConfig().movingTargetMarginBypass + ".change targeting margin (moving)",
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(getTargetingMarginBypassMovingRecorder()),
//                        "current: " + computeCheatConfig().movingTargetMarginBypass + ". opens float recording screen. safe aura, gl"),
                getConfigFloatInputWidget(
                        "targeting margin (static)",
                        computeCheatConfig().staticTargetingMarginBypass,
                        value -> computeCheatConfig().staticTargetingMarginBypass = value,
                        "targeting margin bypass while standing still; invalid input is shown in red"),
                getConfigFloatInputWidget(
                        "targeting margin (moving)",
                        computeCheatConfig().movingTargetMarginBypass,
                        value -> computeCheatConfig().movingTargetMarginBypass = value,
                        "targeting margin bypass while moving; invalid input is shown in red"),
                // codex end
//                getConfigButtonWidget(
//                        "current: " + computeCheatConfig().targetingMarginWidthBypass + ".change targeting margin width",
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(TARGETING_MARGIN_WIDTH_BYPASS_RECORDER),
//                        "current: " + computeCheatConfig().targetingMarginWidthBypass + ". opens float recording screen. default mc is 0, pre-1.14 or whatever is .1. anything higher is just safe aura, gl"),
//                getConfigCheckboxWidget(
//                        "blindness disable",
//                        config.isDarknessDisabled,
//                        is -> config.isDarknessDisabled = is,
//                        "darkness + blindness + nausea"),
//                getConfigCheckboxWidget(
//                        "player waypoints",
//                        config.isPlayerWaypointsEnabled,
//                        is -> config.isPlayerWaypointsEnabled = is,
//                        ""),
//                getConfigCheckboxWidget(
//                        "sneaky reach (beware)",
//                        computeCheatConfig().isSneakyReachEnabled,
//                        is -> computeCheatConfig().isSneakyReachEnabled = is,
//                        ""),
//                getConfigButtonWidget(
//                        "change attack self velocity multiplier",
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(ATTACK_VELOCITY_BYPASS_RECORDER),
//                        "current: " + computeCheatConfig().attackVelocityBypass + ". opens float recording screen. default mc is 0.6. beware of this setting if the mod has been updated and I haven't re-checked it's mixin"),
//                getConfigCheckboxWidget(
//                        "nameplate iron colored leather swap",
//                        config.isNameplateIronLeatherSwapped,
//                        is -> config.isNameplateIronLeatherSwapped = is,
//                        ""),
                getConfigButtonWidget(
                        "reset ally nameplates",
                        () -> {
                            config.nameplateUuids.values().removeIf(each -> each == Config.NameplateTeam.ALLY);
                            config.saveConfig();
                        },
                        ""),
//                getConfigCheckboxWidget(
//                        "player login messaging",
//                        config.isPlayerLoginMessagingEnabled,
//                        is -> config.isPlayerLoginMessagingEnabled = is,
//                        ""),
                getConfigCheckboxWidget(
                        "combat cheats",
                        config.isCheatsEnabled,
                        is -> config.isCheatsEnabled = is,
                        ""),
//                getConfigButtonWidget(
//                        "send potion count chat message",
//                        () -> {
//                            if (!(MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)) {
//                                return;
//                            }
//
//                            int potionCount = 0;
//                            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
//                                if (stack.getItem() instanceof PotionItem &&
//                                        stack.get(DataComponents.POTION_CONTENTS) instanceof PotionContents potionContents &&
//                                        potionContents.is(Potions.STRONG_HEALING)) {
//                                    potionCount += stack.getCount();
//                                }
//                            }
//                            player.connection.sendChat("I have " + potionCount + " health potions");
//                        },
//                        ""),
//                getConfigButtonWidget(
//                        "nearby allies/enemies: " + calculateNearbyPlayerCountString(),
//                        () -> {
//                        },
//                        ""),
                getConfigButtonWidget(
                        "current player waypoint category: " + config.playerWaypointCategory.name(),
                        () -> {
                            Config.PlayerWaypointCategory[] values = Config.PlayerWaypointCategory.values();
                            config.playerWaypointCategory = values[(config.playerWaypointCategory.ordinal() + 1) % values.length];
                            config.saveConfig();
                        },
                        "current: " + config.playerWaypointCategory.name()),
//                getConfigCheckboxWidget(
//                        "missed attack suppression",
//                        config.isAttackSuppressionEnabled,
//                        is -> config.isAttackSuppressionEnabled = is,
//                        ""),
//                getConfigCheckboxWidget(
//                        "auto cobweb",
//                        computeCheatConfig().isAutoCobweb,
//                        is -> computeCheatConfig().isAutoCobweb = is,
//                        "automatically places a web if you're holding right-click. Works more/less consistently depending on cobweb reach setting"),
//                getConfigButtonWidget(
//                        "change cobweb range bypass delta",
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreenAndShow(COBWEB_BYPASS_DELTA_RECORDER),
//                        "current: " + computeCheatConfig().cobwebRangeBypassDelta + ". default: 0. opens float recording screen"),
                getConfigCheckboxWidget(
                        "debug mode",
                        config.isDebugModeEnabled,
                        is -> config.isDebugModeEnabled = is,
                        ""),
                getConfigCheckboxWidget(
                        "parkour cheat",
                        config.isParkourCheatEnabled,
                        is -> config.isParkourCheatEnabled = is,
                        ""),
                getConfigCheckboxWidget(
                        "suppress teammate swings",
                        config.isTeammatesSwingSuppressionEnabled,
                        is -> config.isTeammatesSwingSuppressionEnabled = is,
                        "")
        ));
    }

//    // TODO -> inline?
//    private static String calculateNearbyPlayerCountString() {
//        int nearbyTeammates = 0;
//        List<AbstractClientPlayer> nearbyPlayers = Objects.requireNonNull(MINECRAFT_CLIENT_INSTANCE.level).players();
//        for (Player each : nearbyPlayers) {
//            if (each == MINECRAFT_CLIENT_INSTANCE.player) {
//                nearbyTeammates++;
//            } else if (config.nameplateUuids.get(each.getUUID()) instanceof Config.NameplateTeam team &&
//                    (team == Config.NameplateTeam.ALLY || team == Config.NameplateTeam.FRIENDLY)) {
//                nearbyTeammates++;
//            }
//        }
//        return nearbyTeammates + "/" + (nearbyPlayers.size() - nearbyTeammates);
//    }
}
