package com.example.Screens;

import com.example.Configs.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;

import static com.example.Constants.*;
import static com.example.DelayedClientState.TEXT_RENDERER;
import static com.example.Screens.Utils.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

public class Constants {
    private static CheckboxWidget getConfigCheckboxWidget(
            String text, boolean isChecked, Consumer<Boolean> consumer, String tooltip) {
        return CheckboxWidget.builder(Text.literal(text), TEXT_RENDERER)
//                .pos(x, y)
                .checked(isChecked)
                .callback((v, is) -> {
                    consumer.accept(is);
                    config.saveConfig();

                    // Cheats start
                    serializeJsonBlocking("cheat-configs", cheatConfigs);
                    // Cheats end
                })
                .tooltip(Tooltip.of(Text.literal(tooltip)))
                .build();
    }

    private static ButtonWidget getConfigButtonWidget(
            String title, Runnable onPress, String tooltip) {
        return ButtonWidget.builder(Text.literal(title), v -> {
                    onPress.run();
                    config.saveConfig();
                    // Cheats start
                    serializeJsonBlocking("cheat-configs", cheatConfigs);
                    // Cheats end
                })
//                .position(x, y)
                .tooltip(Tooltip.of(Text.literal(tooltip)))
                .build();
    }

    private static final Screen TARGETING_MARGIN_BYPASS_RECORDER = getDoubleInputScreen(Text.literal("fing"), number -> computeCheatConfig().targetingMarginBypass = number.floatValue());
    private static final Screen ATTACK_VELOCITY_BYPASS_RECORDER = getDoubleInputScreen(Text.literal("fing1"), number -> computeCheatConfig().attackVelocityBypass = number);

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
                        () -> MINECRAFT_CLIENT_INSTANCE.setScreen(TARGETING_MARGIN_BYPASS_RECORDER),
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
                        () -> MINECRAFT_CLIENT_INSTANCE.setScreen(ATTACK_VELOCITY_BYPASS_RECORDER),
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
                        "")
        ));
    }
}
