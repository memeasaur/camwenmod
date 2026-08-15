package com.example.Screens;

import com.example.MouseMovement;
import com.example.mixins.ChatHudMixin;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.example.Configs.CheatConfig.*;
import static com.example.Configs.Config.*;
import static com.example.Constants.*;
import static com.example.DelayedClientState.TEXT_RENDERER;
import static com.example.Screens.Utils.*;
import static com.example.UntitledClient.*;
import static com.example.Utils.*;

public class Constants {
    public static final Screen CONFIG = new Screen(Text.literal("pvputils config")) {
        @Override
        protected void init() {
            int y = 20;
            int x = 20;

            addDrawableChild(getConfigCheckboxWidget("debug mode", x, y, isDebugModeEnabled, is -> isDebugModeEnabled = is, "logs to minecraft chat"));
            y += 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigCheckboxWidget("togglesneak gui", x + xModifier, y, isToggleSneakGuiEnabled, (is) -> isToggleSneakGuiEnabled = is, "modified version of the classic hcf togglesneak's gui"));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("autorun pvp disable", x + xModifier, y, isMovementTogglePvpDisabling, (is) -> isMovementTogglePvpDisabling = is, "disables movement toggle when taking/dealing player damage"));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("movement toggle mirror press cancel", x + xModifier, y, isMovementToggleMirrorPressDisabling, (is) -> isMovementToggleMirrorPressDisabling = is, "disables movement toggle when autorun movement keys are re-pressed"));
            }
            y += 20;

            addDrawableChild(getConfigCheckboxWidget("enable notification noise when attack indicator reaches threshold", x, y, isAttackCooldownNotificationEnabled, is -> isAttackCooldownNotificationEnabled = is, ""));
            y += 20;

            addDrawableChild(getConfigCheckboxWidget("enable warning noise when attack doesn't reach threshold", x, y, isAttackCooldownWarningEnabled, is -> isAttackCooldownWarningEnabled = is, "play warning sound when attack didn't reach threshold"));
            y += 20;

            addDrawableChild(getConfigCheckboxWidget("enable warning noise when attack doesn't knockback or crit", x, y, isSweepAttackWarningEnabled, is -> isSweepAttackWarningEnabled = is, "plays warning sound when sweep hitting"));
            y += 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigButtonWidget("potion enchantment glint revert: " + (currentPotionEnchantmentGlintType.isEmpty() ? "none" : currentPotionEnchantmentGlintType), () -> {
                    currentPotionEnchantmentGlintType = switch (currentPotionEnchantmentGlintType) {
                        case "" -> "1.8";
                        case "1.8" -> "";
                        default -> throw new RuntimeException("potion enchantment glint revert button err");
                    };
                    MINECRAFT_CLIENT_INSTANCE.setScreen(CONFIG);
                }, x + xModifier, y, "reverts potion enchantment glint to 1.8"));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("enable removal of attack hand lowering", x + xModifier, y, isAttackLoweringDisabled, is -> isAttackLoweringDisabled = is, ""));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("enable reverted sharpness particles", x + xModifier, y, isSharpnessParticleReverted, is -> isSharpnessParticleReverted = is, ""));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("enable reverted crit particles", x + xModifier, y, isCritParticleReverted, is -> isCritParticleReverted = is, "warning: don't use this, modern minecraft handles crits differently"));
            }
            y += 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigCheckboxWidget("knockback particles", x + xModifier, y, isKnockbackParticleEnabled, is -> isKnockbackParticleEnabled = is, ""));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("sweep particles", x + xModifier, y, isSweepParticleEnabled, is -> isSweepParticleEnabled = is, ""));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("bleed particles", x + xModifier, y, isBleedParticleEnabled, is -> isBleedParticleEnabled = is, ""));
            }
            y += 20;

            addDrawableChild(getConfigCheckboxWidget("enable attack indicator information widget", x, y, isAttackIndicatorDataEnabled, is -> isAttackIndicatorDataEnabled = is, "shows range and attack cooldown percentage when attacking and swinging, respectively"));
            y += 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigCheckboxWidget("sneak", x + xModifier, y, isSneakEnabled, is -> isSneakEnabled = is, "toggles sneak"));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("sprint", x + xModifier, y, isSprintEnabled, is -> isSprintEnabled = is, "toggles sprint"));
                xModifier += 150;
                // TODO -> do the other movement toggles here, too
                addDrawableChild(getConfigCheckboxWidget("fly boost", x + xModifier, y, isFlyBoostEnabled, is -> isFlyBoostEnabled = is, "sprint while flying to use it"));
                xModifier += 150;
                addDrawableChild(getConfigCheckboxWidget("fake night vision", x + xModifier, y, isFullbrightEnabled, is -> isFullbrightEnabled = is, "gives the same fullbright that night vision gives you"));
                // TODO -> keybind changer
                // TODO -> let mod keybinds be changed here, too
            }
            y += 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigButtonWidget("list changed nameplates", () ->
                        handleAbstractMojangApiNameplateUpdaterScreen((nonnullNetworkHandler) -> {
                            ArrayList<CompletableFuture<nameplateUpdaterEntry>> futureEntries = new ArrayList<>();
                            for (UUID uuid : nameplateUuids.keySet()) {
                                if (nonnullNetworkHandler.getPlayerListEntry(uuid) instanceof PlayerListEntry playerListEntry)
                                    futureEntries
                                            .add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(playerListEntry.getProfile().getName(), uuid)));
                                else
                                    futureEntries
                                            .add(getHandledMojangApiFuture(() -> HTTP_CLIENT.sendAsync(HttpRequest.newBuilder().uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/" + uuid.toString().replace("-", ""))).build(), HttpResponse.BodyHandlers.ofString()))
                                                    .thenApply(response -> new nameplateUpdaterEntry(JsonParser.parseString(response.body()).getAsJsonObject().get("name").getAsString(), uuid)));
                            }
                            return futureEntries;
                        }), x + xModifier, y, "opens nameplate updater, it could take a while to open"));
                xModifier += 150;
                addDrawableChild(ButtonWidget.builder(Text.literal("list online players' nameplates"), button -> {
                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
                                currentNameplateUpdatePlayers = player.networkHandler.getPlayerList().stream()
                                        .map(PlayerListEntry::getProfile)
                                        .map(profile -> new nameplateUpdaterEntry(profile.getName(), profile.getId()))
                                        .toArray(nameplateUpdaterEntry[]::new);
//                                for (GameProfile gameProfile : ) {
////                                    if (playerListEntry.getDisplayName() instanceof Text displayNameText && displayNameText.getString() instanceof String displayNameString && !displayNameString.isEmpty())
//                                    GameProfile profile = playerListEntry.getProfile();
//                                    mutableList.add(new nameplateUpdaterEntry(profile.getName(), profile.getId()));
//                                } TODO remove these
//                                currentNameplateUpdatePlayers = mutableList.toArray(new nameplateUpdaterEntry[0]);
                                MINECRAFT_CLIENT_INSTANCE.setScreen(NAMEPLATE_UPDATER);
                            }
                        })
                        .position(x + xModifier, y)
                        .tooltip(Tooltip.of(Text.literal("opens nameplate updater")))
                        .build());
                xModifier += 150;
                addDrawableChild(ButtonWidget.builder(Text.literal("list matching recent chat message's nameplates"), button ->
                                MINECRAFT_CLIENT_INSTANCE.setScreen(getAbstractKeyboardSequenceScreen(Text.literal("1.) use regular /f who etc. 2.) click this button 3.) type any part of the returned f who message 4.) press esc"), (v) -> true, (inputString, client) -> {
                                    if (inputString.isEmpty())
                                        client.execute(() -> {
                                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                                                player.sendMessage(Text.literal("invalid string"), true); // TODO -> console
                                        });
                                    else // TODO -> clicking the chat message would be nicer, probably
                                        client.execute(() ->
                                                handleAbstractMojangApiNameplateUpdaterScreen((nonnullNetworkHandler) -> {
                                                    StringBuilder stringBuilder = new StringBuilder();
                                                    CharacterVisitor characterVisitor = (index, style, codePoint) -> {
                                                        stringBuilder.append(Character.toChars(codePoint));
                                                        return true;
                                                    };
                                                    var messages = ((ChatHudMixin) MINECRAFT_CLIENT_INSTANCE.inGameHud.getChatHud()).getVisibleMessages();
                                                    boolean flag = false;
                                                    for (int i = messages.size() - 1; i >= 0; i--) {
                                                        var message = messages.get(i);
                                                        if (message.endOfEntry()) {
                                                            if (!flag)
                                                                stringBuilder.setLength(0);
                                                            else
                                                                break;
                                                        }
                                                        message.content().accept(characterVisitor);
                                                        if (stringBuilder.toString().contains(inputString))
                                                            flag = true;
                                                    }
                                                    ArrayList<CompletableFuture<nameplateUpdaterEntry>> futureEntries = new ArrayList<>();
                                                    ArrayList<String> currentNamesBatch = new ArrayList<>();
                                                    Runnable doBatch = () -> {
                                                        JsonArray jsonArray = new JsonArray();
                                                        currentNamesBatch.forEach(jsonArray::add);
                                                        futureEntries
                                                                .add(getHandledMojangApiFuture(() -> HTTP_CLIENT.sendAsync(HttpRequest.newBuilder()
                                                                        .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname"))
                                                                        .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(jsonArray)))
                                                                        .build(), HttpResponse.BodyHandlers.ofString()))
                                                                        .thenCompose(response -> {
                                                                            try {
                                                                                var array = JsonParser.parseString(response.body()).getAsJsonArray();
                                                                                for (JsonElement jsonElement : array) {
                                                                                    var object = jsonElement.getAsJsonObject();
                                                                                    futureEntries.add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(object.get("name").getAsString(), UUID.fromString(object.get("id").getAsString().replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"))))); // TODO -> this regex is chatgpt
                                                                                }
                                                                                return CompletableFuture.completedFuture(null);
                                                                            } catch (Exception e) {
                                                                                MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal(e.getMessage()), false);
                                                                                throw new RuntimeException(e);
                                                                            }
                                                                        }));
                                                    };
                                                    for (String word : stringBuilder.toString().split("\\W+")) {
                                                        if (!word.isEmpty()) {
                                                            if (nonnullNetworkHandler.getPlayerListEntry(word) instanceof PlayerListEntry playerListEntry) {
                                                                GameProfile gameProfile = playerListEntry.getProfile();
                                                                futureEntries
                                                                        .add(CompletableFuture.completedFuture(new nameplateUpdaterEntry(gameProfile.getName(), gameProfile.getId())));
                                                            } else {
                                                                currentNamesBatch.add(word);
                                                                if (currentNamesBatch.size() == 10) {
                                                                    doBatch.run();
                                                                    currentNamesBatch.clear();
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (!currentNamesBatch.isEmpty())
                                                        doBatch.run();
                                                    return futureEntries;
                                                }));
                                }, CONFIG)))
                        .position(x + xModifier, y)
                        .tooltip(Tooltip.of(Text.literal("opens nameplate updater")))
                        .build());
            }
            y += 20;
            {
                int xModifier = 0;
                addDrawableChild(ButtonWidget.builder(Text.literal("create duplicate vanilla keybind"), button ->
                                MINECRAFT_CLIENT_INSTANCE.setScreen(getAbstractKeyboardSequenceScreen(Text.literal("keyboard only: press vanilla keybind then press desired duplicate keybind"), (sequence) -> sequence.length() >= 2, (inputString, client) -> // TODO getting the string here instead of just using the input is retarded/lazy but I guess I am, too
                                        client.execute(() -> {
                                            // TODO
//                                            for (KeyBinding keyBinding : OPTIONS.allKeys)
//                                                if (InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode() == ) )
                                        }), CONFIG)))
                        .position(x + xModifier, y)
                        .tooltip(Tooltip.of(Text.literal("opens keybind recorder")))
                        .build());
            }
        }
    };

    private static CheckboxWidget getConfigCheckboxWidget(String text, int x, int y, boolean isChecked, Consumer<Boolean> consumer, String tooltip) {
        return CheckboxWidget.builder(Text.literal(text), TEXT_RENDERER)
                .pos(x, y)
                .checked(isChecked)
                .callback((v, is) -> {
                    consumer.accept(is);
                    saveConfig();

                    // Cheats start
                    saveCheatConfig();
                    // Cheats end
                })
                .tooltip(Tooltip.of(Text.literal(tooltip)))
                .build();
    }

    private static ButtonWidget getConfigButtonWidget(String title, Runnable onPress, int x, int y, String tooltip) {
        return ButtonWidget.builder(Text.literal(title), v -> {
                    onPress.run();
                    saveConfig();
                    // Cheats start
                    saveCheatConfig();
                    // Cheats end
                })
                .position(x, y)
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
                String team = nameplateUuids.getOrDefault(uuid, "neutral");
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
    public static final Screen CHEAT_CONFIG = new Screen(Text.literal("cheat config")) {
        @Override
        protected void init() {
            int y = 20;
            int x = 20;

            {
                int xModifier = 0;
                addDrawableChild(getConfigButtonWidget("record autoclick macro", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_MACRO_RECORDER), x + xModifier, y, "opens blank recording screen"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("list recorded autoclick macros", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(RECORDED_AUTOCLICKERS_MANAGER), x + xModifier, y, "lists all current recorded autoclickers"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("change autoclick starting multipler. current: " + autoclickerStartingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_STARTING_MULTIPLIER_RECORDER), x + xModifier, y, "opens float recording screen. current: " + autoclickerStartingMultiplier));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("change autoclick ending multiplier. current: " + autoclickerEndingMultiplier, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_ENDING_MULTIPLIER_RECORDER), x + xModifier, y, "opens float recording screen. current: " + autoclickerEndingMultiplier));
            }
            y += 20;
            {
                int xModifier = 0;
                addDrawableChild(getConfigButtonWidget("change autoclick toggle keybind. current: " + glfwToggleAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_TOGGLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("change autoclick enable keybind. current: " + glfwEnableAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_ENABLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("change autoclick disable keybind. current: " + glfwDisableAutoclickerKeybind, () -> MINECRAFT_CLIENT_INSTANCE.setScreen(AUTOCLICK_DISABLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
            }
            y += 20;
            {
                int xModifier = 0;
                addDrawableChild(getConfigButtonWidget("change player xray toggle keybind", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(PLAYER_XRAY_TOGGLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget("change block xray toggle keybind", () -> MINECRAFT_CLIENT_INSTANCE.setScreen(BLOCK_XRAY_TOGGLE_KEYBIND_RECORDER), x + xModifier, y, "opens keybind recorder screen"));
            }
            y += 20;
            {
                int xModifier = 0;
                addDrawableChild(getConfigCheckboxWidget(
                        "ethylene",
                        x + xModifier,
                        y,
                        isEthylene,
                        is -> isEthylene = is,
                        "shotbow lol"));
//                xModifier += 150;
//                addDrawableChild(getConfigCheckboxWidget(
//                        "automatic w tap",
//                        x + xModifier,
//                        y,
//                        isAutomaticWTapping,
//                        is -> isAutomaticWTapping = is,
//                        "shotbow lol"));
//                xModifier += 150;
//                addDrawableChild(getConfigCheckboxWidget(
//                        "cobweb autoclicker",
//                        x + xModifier,
//                        y,
//                        isAutoCobweb,
//                        is -> isAutoCobweb = is,
//                        "shotbow lol"));
                xModifier += 150;
                addDrawableChild(getConfigButtonWidget(
                        "change autoclick starting multipler. current: " + targetingMarginBypass,
                        () -> MINECRAFT_CLIENT_INSTANCE.setScreen(TARGETING_MARGIN_BYPASS_RECORDER),
                        x + xModifier,
                        y,
                        "opens float recording screen. default mc is 0, pre-1.14 or whatever is .1. anything higher is just safe aura, gl"));
            }
        }
    };

    static {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Object AUTOCLICK_MACRO_RECORDER_LOCK = new Object();
    private static final Text AUTOCLICK_MACRO_RECORDER_TITLE = Text.literal("doBatch");
    private static final Screen AUTOCLICK_MACRO_RECORDER = getAbstractInputScreen(AUTOCLICK_MACRO_RECORDER_TITLE, (threadClientInstance) -> {
        try {
            ArrayList<Integer> mutableMacroClicks = new ArrayList<>();
            long[] lastClickEventNanoseconds = new long[]{System.nanoTime()};
            BiConsumer<long[], ArrayList<Integer>> handleAutoclickMacroRecorderIteration = (nanoseconds, macroList) -> {
                long currentNanoseconds = System.nanoTime();
                long delay = currentNanoseconds - lastClickEventNanoseconds[0];
                if (delay > Integer.MAX_VALUE ||
                        (!(threadClientInstance.currentScreen instanceof Screen screen) || !screen.getTitle().equals(AUTOCLICK_MACRO_RECORDER_TITLE)))
                    synchronized (AUTOCLICK_MACRO_RECORDER_LOCK) {
                        AUTOCLICK_MACRO_RECORDER_LOCK.notify();
                    }
                else {
                    mutableMacroClicks.add((int) delay);
                    lastClickEventNanoseconds[0] = currentNanoseconds;
                }
            };
            NativeMouseListener nativeMouseListener = new NativeMouseListener() {
                @Override
                public void nativeMousePressed(NativeMouseEvent e) { // TODO -> queue these? APPARENTLY jnativehook processes these synchronously (?)
                    if (e.getButton() == NativeMouseEvent.BUTTON1)
                        handleAutoclickMacroRecorderIteration.accept(lastClickEventNanoseconds, mutableMacroClicks);
                }

                @Override
                public void nativeMouseReleased(NativeMouseEvent e) {
                    if (e.getButton() == NativeMouseEvent.BUTTON1 && !mutableMacroClicks.isEmpty())
                        handleAutoclickMacroRecorderIteration.accept(lastClickEventNanoseconds, mutableMacroClicks);
                }
            };

            ArrayList<MouseMovement> mutableMacroMovements = new ArrayList<>();
            NativeMouseMotionListener nativeMouseMotionListener = new NativeMouseMotionListener() {
                int lastX = 0;
                int lastY = 0;
                long lastMoveEventNanoseconds = System.nanoTime();

                @Override
                public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
                    int x = nativeEvent.getX();
                    int y = nativeEvent.getY();
//                    NativeMouseMotionListener.super.nativeMouseMoved(nativeEvent); TODO remove?
                    long currentNanoseconds = System.nanoTime();
                    if (mutableMacroClicks.size() > 2) // TODO -> this will track for longer than the duration of the recorded clicks, this is annoying since it gets reversed, but I guess I'm remedying this by messaging the reversal to the fake mouse movement hook
                        mutableMacroMovements.add(new MouseMovement((int) (currentNanoseconds - lastMoveEventNanoseconds), x - lastX, y - lastY));
                    lastX = x;
                    lastY = y;
                    lastMoveEventNanoseconds = currentNanoseconds;
                }
            };
            GlobalScreen.addNativeMouseListener(nativeMouseListener);
            GlobalScreen.addNativeMouseMotionListener(nativeMouseMotionListener);
            synchronized (AUTOCLICK_MACRO_RECORDER_LOCK) {
                AUTOCLICK_MACRO_RECORDER_LOCK.wait();
            }
            Thread.sleep(10); // TODO just queue them and wait for them all to be absolutely finished
            GlobalScreen.removeNativeMouseListener(nativeMouseListener);
            GlobalScreen.removeNativeMouseMotionListener(nativeMouseMotionListener);
            int[] newRecordedAutoclickerClicks;
            if (!mutableMacroClicks.isEmpty()) {
                int oldLength = immutableRecordedAutoclickerClicks.length;
                {
                    int newLength = oldLength + 1;
                    {
                        int[][] newMatrix = new int[newLength][];
                        System.arraycopy(immutableRecordedAutoclickerClicks, 0, newMatrix, 0, oldLength);
                        immutableRecordedAutoclickerClicks = newMatrix;
                    }
                    {
                        MouseMovement[][] newMatrix1 = new MouseMovement[newLength][];
                        System.arraycopy(immutableRecordedAutoclickerMovements, 0, newMatrix1, 0, oldLength);
                        immutableRecordedAutoclickerMovements = newMatrix1;
                    }
                }
                newRecordedAutoclickerClicks = mutableMacroClicks.stream()
                        .skip(2)
                        .mapToInt(Integer::intValue)
                        .toArray();
                immutableRecordedAutoclickerClicks[oldLength] = newRecordedAutoclickerClicks;

                MouseMovement[] newRecordedAutoclickerMovements = mutableMacroMovements.toArray(new MouseMovement[0]);
                immutableRecordedAutoclickerMovements[oldLength] = newRecordedAutoclickerMovements;
            } else
                newRecordedAutoclickerClicks = null;
            threadClientInstance.execute(() -> {
                if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                    player.sendMessage(newRecordedAutoclickerClicks != null
                            ? getAutoclickerText(newRecordedAutoclickerClicks)
                            : Text.literal("invalid macro"), true); // TODO -> console
            });
        } catch (Exception e) {
            threadClientInstance.execute(() -> {
                if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                    player.sendMessage(Text.literal(e.getMessage()), false);
            });
        }
    }, CHEAT_CONFIG);
    private static final Screen AUTOCLICK_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> glfwToggleAutoclickerKeybind = key, CHEAT_CONFIG);
    private static final Screen AUTOCLICK_ENABLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> glfwEnableAutoclickerKeybind = key, CHEAT_CONFIG);
    private static final Screen AUTOCLICK_DISABLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("bar"), (key) -> glfwDisableAutoclickerKeybind = key, CHEAT_CONFIG);
    private static final Screen AUTOCLICK_STARTING_MULTIPLIER_RECORDER = getFloatInputScreen(Text.literal("change autoclicker starting multiplier (" + autoclickerStartingMultiplier + ")"), number -> autoclickerStartingMultiplier = number, CHEAT_CONFIG);
    private static final Screen AUTOCLICK_ENDING_MULTIPLIER_RECORDER = getFloatInputScreen(Text.literal("change autoclicker ending multiplier (" + autoclickerEndingMultiplier + ")"), number -> autoclickerEndingMultiplier = number, CHEAT_CONFIG);
    private static final Screen RECORDED_AUTOCLICKERS_MANAGER = new Screen(Text.literal("baz")) {
        @Override
        protected void init() {
            int y = 20;
            int originalLength = immutableRecordedAutoclickerClicks.length;
            for (int[] macro : immutableRecordedAutoclickerClicks) {
                addDrawableChild(ButtonWidget.builder(getAutoclickerText(macro), (button) -> {
                            int[][] newMatrix = new int[originalLength - 1][];
                            int i = 0;
                            for (int[] macro1 : immutableRecordedAutoclickerClicks)
                                if (macro != macro1)
                                    newMatrix[i++] = macro1;
                            immutableRecordedAutoclickerClicks = newMatrix;
                            MINECRAFT_CLIENT_INSTANCE.setScreen(RECORDED_AUTOCLICKERS_MANAGER);
                        })
                        .position(20, y += 20)
                        .tooltip(Tooltip.of(Text.literal("click to delete")))
                        .build());
            }
        }
    };

    private static final Screen PLAYER_XRAY_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("fang"), (key) -> glfwTogglePlayerXrayKeybind = key, CHEAT_CONFIG);
    private static final Screen BLOCK_XRAY_TOGGLE_KEYBIND_RECORDER = getAbstractKeybindInputScreen(Text.literal("fong"), (key) -> glfwToggleBlockXrayKeybind = key, CHEAT_CONFIG);
    private static final Screen TARGETING_MARGIN_BYPASS_RECORDER = getFloatInputScreen(Text.literal("fing"), number -> targetingMarginBypass = number, CHEAT_CONFIG);
    // Cheats end
}
