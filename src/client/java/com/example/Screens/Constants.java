package com.example.Screens;

import com.example.Configs.Config;
import com.github.kwhat.jnativehook.GlobalScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextWidget;
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

    record nameplateUpdaterEntry(String name, UUID uuid) {
    }

    static nameplateUpdaterEntry[] currentNameplateUpdatePlayers;
    private static final Text
            ALLY_TEXT = Text.literal("ally"),
            ENEMY_TEXT = Text.literal("enemy"),
            FOCUS_TEXT = Text.literal("focus"),
            NEUTRAL_TEXT = Text.literal("neutral");
    private static int nameplateUpdaterPage = 1;
    public static final Screen NAMEPLATE_UPDATER = new Screen(Text.literal("nameplate updater")) { // TODO -> keybind for this
        final Consumer<Integer> handlePagination = (page) -> {
            nameplateUpdaterPage = page;
            MINECRAFT_CLIENT_INSTANCE.setScreen(NAMEPLATE_UPDATER);
            nameplateUpdaterPage = 1;
        };

        @Override
        protected void init() {
            int currentPage = nameplateUpdaterPage;
            int y = 0;
            int startingI = (currentPage - 1) * 15;

            {
                Consumer<String> handle = (teamString) -> new Thread(() -> {
                    for (nameplateUpdaterEntry foo : currentNameplateUpdatePlayers)
                        putNameplateUuidEntry(Map.entry(foo.uuid, teamString)); // TODO -> make ally string enum or constant or something
                    MinecraftClient.getInstance().execute(() -> handlePagination.accept(currentPage));
                }).start();
                int x = 0;
                addDrawableChild(ButtonWidget.builder(Text.literal("ally all"), (v) ->
                                handle.accept("ally"))
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(Text.literal("enemy all"), (v) ->
                                handle.accept("enemy"))
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(Text.literal("focus all"), (v) ->
                                handle.accept("focus"))
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(Text.literal("neutral all"), (v) -> new Thread(() -> {
                            for (nameplateUpdaterEntry foo : currentNameplateUpdatePlayers)
                                removeNameplateUuidEntry(foo.uuid);
                            MinecraftClient.getInstance().execute(() -> handlePagination.accept(currentPage));
                        }).start())
                        .position(x, y)
                        .build());
            }
            y += 20;

            final int perPage = 10;
            for (int i = startingI; i < Math.min(currentNameplateUpdatePlayers.length, startingI + perPage); i++) {
                nameplateUpdaterEntry playerListEntry = currentNameplateUpdatePlayers[i];
                UUID uuid = playerListEntry.uuid;
                String team = config.nameplateUuids.getOrDefault(uuid, "neutral");
                var textWidget = new TextWidget(Text.literal(playerListEntry.name), TEXT_RENDERER);
                textWidget.setPosition(100, y);
                textWidget.setTextColor(switch (team) {
                    case "neutral" -> YELLOW_RGB;
                    case "ally" -> AQUA_RGB;
                    case "enemy" -> RED_RGB;
                    case "focus" -> LIGHT_PURPLE_RGB;
                    default -> throw new RuntimeException("nameplate color error 1");
                });
                int x = 0;
                y += 10;
                addDrawableChild(textWidget);
                addDrawableChild(ButtonWidget.builder(ALLY_TEXT, (v) -> {
                            textWidget.setTextColor(AQUA_RGB);
                            putNameplateUuidEntry(Map.entry(uuid, "ally"));
                        })
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(ENEMY_TEXT, (v) -> {
                            textWidget.setTextColor(RED_RGB);
                            putNameplateUuidEntry(Map.entry(uuid, "enemy"));
                        })
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(FOCUS_TEXT, (v) -> {
                            textWidget.setTextColor(LIGHT_PURPLE_RGB);
                            putNameplateUuidEntry(Map.entry(uuid, "focus"));
                        })
                        .position(x, y)
                        .build());
                x += 150;
                addDrawableChild(ButtonWidget.builder(NEUTRAL_TEXT, (v) -> {
                            textWidget.setTextColor(YELLOW_RGB);
                            removeNameplateUuidEntry(uuid);
                        })
                        .position(x, y)
                        .build());
                x += 150;
                y += 20;
            }
            y += 20;
            if (currentPage > 1)
                addDrawableChild(ButtonWidget.builder(Text.literal("previous page"), (v) -> {
                            nameplateUpdaterPage = currentPage - 1;
                            MINECRAFT_CLIENT_INSTANCE.setScreen(NAMEPLATE_UPDATER);
                            nameplateUpdaterPage = 1;
                        })
                        .position(20, y)
                        .build());
            if (startingI + 15 < currentNameplateUpdatePlayers.length)
                addDrawableChild(ButtonWidget.builder(Text.literal("next page"), (v) ->
                                handlePagination.accept(currentPage + 1))
                        .position(120, y)
                        .build());
        }
    };

    // Cheats start
    private static final Screen AUTOCLICK_JITTER_MACRO_RECORDER = BuildAutoclickMacroRecorderScreen(Text.literal("doBatch"), false);
    private static final Screen AUTOCLICK_SLOW_MACRO_RECORDER = BuildAutoclickMacroRecorderScreen(Text.literal("doBatchSlow"), true);
    private static final Screen AUTOCLICK_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> config.glfwToggleAutoclickerKeybind = key);
    private static final Screen AUTOCLICK_ENABLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> config.glfwEnableAutoclickerKeybind = key);
    private static final Screen AUTOCLICK_DISABLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> config.glfwDisableAutoclickerKeybind = key);
    private static final Screen AUTOCLICK_JITTER_STARTING_MULTIPLIER_RECORDER = getDoubleInputScreen(Text.literal("change autoclicker starting multiplier (" + config.autoclickerJitterStartingMultiplier + ")"), number -> config.autoclickerJitterStartingMultiplier = number.floatValue());
    private static final Screen AUTOCLICK_JITTER_ENDING_MULTIPLIER_RECORDER = getDoubleInputScreen(Text.literal("change autoclicker ending multiplier (" + config.autoclickerJitterEndingMultiplier + ")"), number -> config.autoclickerJitterEndingMultiplier = number.floatValue());
    private static final Screen AUTOCLICK_SLOW_STARTING_MULTIPLIER_RECORDER = getDoubleInputScreen(Text.literal("change autoclicker starting multiplier (" + config.autoclickerJitterStartingMultiplier + ")"), number -> config.autoclickerSlowStartingMultiplier = number.floatValue());
    private static final Screen AUTOCLICK_SLOW_ENDING_MULTIPLIER_RECORDER = getDoubleInputScreen(Text.literal("change autoclicker ending multiplier (" + config.autoclickerJitterEndingMultiplier + ")"), number -> config.autoclickerSlowEndingMultiplier = number.floatValue());
    private static final Screen RECORDED_AUTOCLICKERS_MANAGER = new Screen(Text.literal("baz")) {
        @Override
        protected void init() {
            int y = 20;
            for (Config.ClickRecording clickRecording : config.recordedClickSequences) {
                addDrawableChild(ButtonWidget.builder(
                                getAutoclickerText(clickRecording.clicks()),
                                (button) -> {
                                    config.recordedClickSequences.remove(clickRecording); // TODO ?
                                    MINECRAFT_CLIENT_INSTANCE.setScreen(RECORDED_AUTOCLICKERS_MANAGER);
                                })
                        .position(20, y += 20)
                        .tooltip(Tooltip.of(Text.literal("click to delete")))
                        .build());
            }
        }
    };

    private static final Screen PLAYER_XRAY_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("fang"), (key) -> config.glfwTogglePlayerXrayKeybind = key);
    private static final Screen BLOCK_XRAY_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("fong"), (key) -> config.glfwToggleBlockXrayKeybind = key);
    private static final Screen TARGETING_MARGIN_BYPASS_RECORDER = getDoubleInputScreen(Text.literal("fing"), number -> computeCheatConfig().targetingMarginBypass = number.floatValue());
    private static final Screen ATTACK_VELOCITY_BYPASS_RECORDER = getDoubleInputScreen(Text.literal("fing1"), number -> computeCheatConfig().attackVelocityBypass = number);

//    private static final Screen RANDOM_DOUBLE_CLICK_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("fpng"), (key) -> glfwToggleRandomDoubleClickKeybind = key, CHEAT_CONFIG);

    // TODO -> let mod keybinds be changed here, too
    // TODO -> do the other movement toggles here, too
    public static final Screen CONFIG = buildConfigScreen("pvputils config", List.of(
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
            getConfigButtonWidget(
                    "change player xray toggle keybind",
                    () -> MINECRAFT_CLIENT_INSTANCE.setScreen(PLAYER_XRAY_TOGGLE_KEYBIND_RECORDER),
                    "opens keybind recorder screen"),
            getConfigButtonWidget("change block xray toggle keybind", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(BLOCK_XRAY_TOGGLE_KEYBIND_RECORDER), "opens keybind recorder screen"),
            getConfigCheckboxWidget(
                    "ethylene",
                    computeCheatConfig().isEthylene,
                    is -> computeCheatConfig().isEthylene = is,
                    "shotbow lol"),
            getConfigButtonWidget(
                    "change targeting margin",
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
                    "grapple ground check",
                    config.isGrappleGroundCheckEnabled,
                    is -> config.isGrappleGroundCheckEnabled = is,
                    ""),
            getConfigCheckboxWidget(
                    "team hit messaging",
                    config.isTeamHitMessagingEnabled,
                    is -> config.isTeamHitMessagingEnabled = is,
                    "")
    ));
//            addDrawableChild(getConfigCheckboxWidget("debug mode", x, y, isDebugModeEnabled, is -> isDebugModeEnabled = is, "logs to minecraft chat"));
//            addDrawableChild(getConfigCheckboxWidget("enable notification noise when attack indicator reaches threshold", x, y, config.isAttackCooldownNotificationEnabled, is -> config.isAttackCooldownNotificationEnabled = is, ""));
//            addDrawableChild(getConfigCheckboxWidget("enable warning noise when attack doesn't reach threshold", x, y, config.isAttackCooldownWarningEnabled, is -> config.isAttackCooldownWarningEnabled = is, "play warning sound when attack didn't reach threshold"));
//            addDrawableChild(getConfigCheckboxWidget("enable warning noise when attack doesn't knockback or crit", x, y, config.isSweepAttackWarningEnabled, is -> config.isSweepAttackWarningEnabled = is, "plays warning sound when sweep hitting"));
//                addDrawableChild(getConfigButtonWidget("potion enchantment glint revert: " + (config.currentPotionEnchantmentGlintType.isEmpty() ? "none" : config.currentPotionEnchantmentGlintType), () -> {
//                    config.currentPotionEnchantmentGlintType = switch (config.currentPotionEnchantmentGlintType) {
//                        case "" -> "1.8";
//                        case "1.8" -> "";
//                        default -> throw new RuntimeException("potion enchantment glint revert button err");
//                    };
//                    MINECRAFT_CLIENT_INSTANCE.setScreen(CONFIG);
//                }, x + xModifier, y, "reverts potion enchantment glint to 1.8"));
//                addDrawableChild(getConfigCheckboxWidget("enable removal of attack hand lowering", x + xModifier, y, config.isAttackLoweringDisabled, is -> config.isAttackLoweringDisabled = is, ""));
//                addDrawableChild(getConfigCheckboxWidget("enable reverted sharpness particles", x + xModifier, y, config.isSharpnessParticleReverted, is -> config.isSharpnessParticleReverted = is, ""));
//                addDrawableChild(getConfigCheckboxWidget("enable reverted crit particles", x + xModifier, y, config.isCritParticleReverted, is -> config.isCritParticleReverted = is, "warning: don't use this, modern minecraft handles crits differently"));
//                addDrawableChild(getConfigCheckboxWidget("knockback particles", x + xModifier, y, config.isKnockbackParticleEnabled, is -> config.isKnockbackParticleEnabled = is, ""));
//                addDrawableChild(getConfigCheckboxWidget("sweep particles", x + xModifier, y, config.isSweepParticleEnabled, is -> config.isSweepParticleEnabled = is, ""));
//                addDrawableChild(getConfigCheckboxWidget("bleed particles", x + xModifier, y, config.isBleedParticleEnabled, is -> config.isBleedParticleEnabled = is, ""));
//            addDrawableChild(getConfigCheckboxWidget("enable attack indicator information widget", x, y, config.isAttackIndicatorDataEnabled, is -> config.isAttackIndicatorDataEnabled = is, "shows range and attack cooldown percentage when attacking and swinging, respectively"));
//                addDrawableChild(getConfigCheckboxWidget("fly boost", x + xModifier, y, config.isFlyBoostEnabled, is -> config.isFlyBoostEnabled = is, "sprint while flying to use it"));
//                addDrawableChild(getConfigButtonWidget("list changed nameplates", () ->
//                        handleAbstractMojangApiNameplateUpdaterScreen((nonnullNetworkHandler) -> {
//                            ArrayList<CompletableFuture<nameplateUpdaterEntry>> futureEntries = new ArrayList<>();
//                            for (UUID uuid : config.nameplateUuids.keySet()) {
//                                if (nonnullNetworkHandler.getPlayerListEntry(uuid) instanceof PlayerListEntry playerListEntry)
//                                    futureEntries
//                                            .add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(playerListEntry.getProfile().getName(), uuid)));
//                                else
//                                    futureEntries
//                                            .add(getHandledMojangApiFuture(() -> HTTP_CLIENT.sendAsync(HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/" + uuid.toString().replace("-", ""))).build(), HttpResponse.BodyHandlers.ofString()))
//                                                    .thenApply(response -> new nameplateUpdaterEntry(JsonParser.parseString(response.body()).getAsJsonObject().get("name").getAsString(), uuid)));
//                            }
//                            return futureEntries;
//                        }), x + xModifier, y, "opens nameplate updater, it could take a while to open"));
//                addDrawableChild(ButtonWidget.builder(Text.literal("list online players' nameplates"), button -> {
//                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
//                                currentNameplateUpdatePlayers = player.networkHandler.getPlayerList().stream()
//                                        .map(PlayerListEntry::getProfile)
//                                        .map(profile -> new nameplateUpdaterEntry(profile.getName(), profile.getId()))
//                                        .toArray(nameplateUpdaterEntry[]::new);
////                                for (GameProfile gameProfile : ) {
//////                                    if (playerListEntry.getDisplayName() instanceof Text displayNameText && displayNameText.getString() instanceof String displayNameString && !displayNameString.isEmpty())
////                                    GameProfile profile = playerListEntry.getProfile();
////                                    mutableList.add(new nameplateUpdaterEntry(profile.getName(), profile.getId()));
////                                } TODO remove these
////                                currentNameplateUpdatePlayers = mutableList.toArray(new nameplateUpdaterEntry[0]);
//                                MINECRAFT_CLIENT_INSTANCE.setScreen(NAMEPLATE_UPDATER);
//                            }
//                        })
//                        .position(x + xModifier, y)
//                        .tooltip(Tooltip.of(Text.literal("opens nameplate updater")))
//                        .build());
//                addDrawableChild(
//                        ButtonWidget.builder(Text.literal("list matching recent chat message's nameplates"),
//                                        button ->
//                                                MINECRAFT_CLIENT_INSTANCE.setScreen(getAbstractKeyboardSequenceScreen(Text.literal("1.) use regular /f who etc. 2.) click this button 3.) type any part of the returned f who message 4.) press esc"), (v) -> true, (inputString, client) -> {
//                                                    if (inputString.isEmpty())
//                                                        client.execute(() -> {
//                                                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
//                                                                player.sendMessage(Text.literal("invalid string"), true); // TODO -> console
//                                                        });
//                                                    else // TODO -> clicking the chat message would be nicer, probably
//                                                        client.execute(() ->
//                                                                handleAbstractMojangApiNameplateUpdaterScreen((nonnullNetworkHandler) -> {
//                                                                    StringBuilder stringBuilder = new StringBuilder();
//                                                                    CharacterVisitor characterVisitor = (index, style, codePoint) -> {
//                                                                        stringBuilder.append(Character.toChars(codePoint));
//                                                                        return true;
//                                                                    };
//                                                                    var messages = ((ChatHudMixin) MINECRAFT_CLIENT_INSTANCE.inGameHud.getChatHud()).getVisibleMessages();
//                                                                    boolean flag = false;
//                                                                    for (int i = messages.size() - 1; i >= 0; i--) {
//                                                                        var message = messages.get(i);
//                                                                        if (message.endOfEntry()) {
//                                                                            if (!flag)
//                                                                                stringBuilder.setLength(0);
//                                                                            else
//                                                                                break;
//                                                                        }
//                                                                        message.content().accept(characterVisitor);
//                                                                        if (stringBuilder.toString().contains(inputString))
//                                                                            flag = true;
//                                                                    }
//                                                                    ArrayList<CompletableFuture<nameplateUpdaterEntry>> futureEntries = new ArrayList<>();
//                                                                    ArrayList<String> currentNamesBatch = new ArrayList<>();
//                                                                    Runnable doBatch = () -> {
//                                                                        JsonArray jsonArray = new JsonArray();
//                                                                        currentNamesBatch.forEach(jsonArray::add);
//                                                                        futureEntries
//                                                                                .add(getHandledMojangApiFuture(() -> HTTP_CLIENT.sendAsync(HttpRequest.newBuilder()
//                                                                                        .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname"))
//                                                                                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonArray)))
//                                                                                        .build(), HttpResponse.BodyHandlers.ofString()))
//                                                                                        .thenCompose(response -> {
//                                                                                            try {
//                                                                                                var array = JsonParser.parseString(response.body()).getAsJsonArray();
//                                                                                                for (JsonElement jsonElement : array) {
//                                                                                                    var object = jsonElement.getAsJsonObject();
//                                                                                                    futureEntries.add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(object.get("name").getAsString(), UUID.fromString(object.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"))))); // TODO -> this regex is chatgpt
//                                                                                                }
//                                                                                                return CompletableFuture.completedFuture(null);
//                                                                                            } catch (Exception e) {
//                                                                                                MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal(e.getMessage()), false);
//                                                                                                throw new RuntimeException(e);
//                                                                                            }
//                                                                                        }));
//                                                                    };
//                                                                    for (String word : stringBuilder.toString().split("\\W+")) {
//                                                                        if (!word.isEmpty()) {
//                                                                            if (nonnullNetworkHandler.getPlayerListEntry(word) instanceof PlayerListEntry playerListEntry) {
//                                                                                GameProfile gameProfile = playerListEntry.getProfile();
//                                                                                futureEntries
//                                                                                        .add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(gameProfile.getName(), gameProfile.getId())));
//                                                                            } else {
//                                                                                currentNamesBatch.add(word);
//                                                                                if (currentNamesBatch.size() == 10) {
//                                                                                    doBatch.run();
//                                                                                    currentNamesBatch.clear();
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                    if (!currentNamesBatch.isEmpty())
//                                                                        doBatch.run();
//                                                                    return futureEntries;
//                                                                }));
//                                                }, CONFIG)))
//                                .position(x + xModifier, y)
//                                .tooltip(Tooltip.of(Text.literal("opens nameplate updater")))
//                                .build());
//                addDrawableChild(ButtonWidget.builder(Text.literal("create duplicate vanilla keybind"), button ->
//                                MINECRAFT_CLIENT_INSTANCE.setScreen(getAbstractKeyboardSequenceScreen(Text.literal("keyboard only: press vanilla keybind then press desired duplicate keybind"), (sequence) -> sequence.length() >= 2, (inputString, client) -> // TODO getting the string here instead of just using the input is retarded/lazy but I guess I am, too
//                                        client.execute(() -> {
//                                            // TODO

    /// /                                            for (KeyBinding keyBinding : OPTIONS.allKeys)
    /// /                                                if (InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode() == ) )
//                                        }), CONFIG)))
//                        .position(x + xModifier, y)
//                        .tooltip(Tooltip.of(Text.literal("opens keybind recorder")))
//                        .build());
    public static final Screen CHEAT_CONFIG = buildConfigScreen("cheat config", List.of(
            getConfigButtonWidget("list recorded autoclick macros", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(RECORDED_AUTOCLICKERS_MANAGER), "lists all current recorded autoclickers"),
            getConfigButtonWidget("record jitter autoclick macro", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_JITTER_MACRO_RECORDER), "opens blank recording screen"),
            getConfigButtonWidget("change jitter autoclick starting multiplier. current: " + config.autoclickerJitterStartingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_JITTER_STARTING_MULTIPLIER_RECORDER), "opens float recording screen. current: " + config.autoclickerJitterStartingMultiplier),
            getConfigButtonWidget("change jitter autoclick ending multiplier. current: " + config.autoclickerJitterEndingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_JITTER_ENDING_MULTIPLIER_RECORDER), "opens float recording screen. current: " + config.autoclickerJitterEndingMultiplier),
            getConfigButtonWidget("record slow autoclick macro", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_SLOW_MACRO_RECORDER), "opens blank recording screen"),
            getConfigButtonWidget("change slow autoclick starting multiplier. current: " + config.autoclickerSlowStartingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_SLOW_STARTING_MULTIPLIER_RECORDER), "opens float recording screen. current: " + config.autoclickerSlowStartingMultiplier),
            getConfigButtonWidget("change slow autoclick ending multiplier. current: " + config.autoclickerSlowEndingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_SLOW_ENDING_MULTIPLIER_RECORDER), "opens float recording screen. current: " + config.autoclickerSlowEndingMultiplier),
            getConfigButtonWidget("change autoclick toggle keybind. current: " + config.glfwToggleAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_TOGGLE_KEYBIND_RECORDER), "opens keybind recorder screen"),
            getConfigCheckboxWidget(
                    "autoclick shake",
                    config.isAutoclickerShakeEnabled,
                    is -> config.isAutoclickerShakeEnabled = is,
                    "recorded autoclicker mouse movement"),
            getConfigCheckboxWidget(
                    "cobweb autoclicker",
                    config.isAutoCobweb,
                    is -> config.isAutoCobweb = is,
                    "shotbow lol"),
            getConfigCheckboxWidget(
                    "random double click",
                    isRandomDoubleClickEnabled,
                    is -> {
                        isRandomDoubleClickEnabled = is;
                        if (isRandomDoubleClickEnabled) {
                            GlobalScreen.addNativeMouseListener(RANDOM_DOUBLE_CLICK_LISTENER);
                        } else {
                            GlobalScreen.removeNativeMouseListener(RANDOM_DOUBLE_CLICK_LISTENER);
                        }
                    },
                    ""),
            getConfigCheckboxWidget(
                    "inventory autoclicker",
                    config.isAutoClickInventoryEnabled,
                    is -> config.isAutoClickInventoryEnabled = is,
                    "")
            //                addDrawableChild(getConfigButtonWidget("change autoclick enable keybind. current: " + cheatConfig.glfwEnableAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_ENABLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
//                addDrawableChild(getConfigButtonWidget("change autoclick disable keybind. current: " + cheatConfig.glfwDisableAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_DISABLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
//                addDrawableChild(getConfigCheckboxWidget(
//                        "automatic w tap",
//                        x + xModifier,
//                        y,
//                        isAutomaticWTapping,
//                        is -> isAutomaticWTapping = is,
//                        "shotbow lol"));
//                addDrawableChild(getConfigButtonWidget(
//                        "change random double click keybind. current: : " + glfwToggleRandomDoubleClickKeybind,
//                        () -> MINECRAFT_CLIENT_INSTANCE.setScreen(RANDOM_DOUBLE_CLICK_TOGGLE_KEYBIND_RECORDER),
//                        x + xModifier,
//                        y,
//                        "opens keybind recorder screen"));
    ));
// Cheats end
}
