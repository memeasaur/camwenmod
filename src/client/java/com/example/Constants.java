package com.example;

import com.example.mixins.MouseMixin;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.net.http.HttpClient;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.LockSupport;

import static com.example.Configs.CheatConfig.*;
import static com.example.DelayedClientState.ATTACK_VANILLA;
import static com.example.UntitledClient.*;
import static com.example.Utils.getIsKeyPressed;
import static org.joml.Math.lerp;

public class Constants {
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new Gson();
    public static final Random RANDOM = new Random(); // TODO remove
    public static final MinecraftClient MINECRAFT_CLIENT_INSTANCE = MinecraftClient.getInstance(); // TODO -> move to constants
    public static final byte FLY_BOOST_MULTIPLIER = 4;
    public static final int
            AQUA_RGB = TextColor.fromFormatting(Formatting.AQUA).getRgb(),
            RED_RGB = TextColor.fromFormatting(Formatting.RED).getRgb(),
            LIGHT_PURPLE_RGB = TextColor.fromFormatting(Formatting.LIGHT_PURPLE).getRgb(),
            YELLOW_RGB = TextColor.fromFormatting(Formatting.YELLOW).getRgb();
    public static final float KNOCKBACK_ATTACK_STRENGTH = .9f;
    public static final Text EMPTY_TEXT = Text.literal("");
    public static final Style GREEN_TEXT_STYLE = Style.EMPTY.withColor(Formatting.GREEN);
    public static final Style RED_TEXT_STYLE = Style.EMPTY.withColor(Formatting.RED);

    // Cheats start
    @Nullable
    public static Thread nullableCurrentHeldAutoclickerTask = null;
    public static final NativeMouseListener AUTOCLICKER_MOUSE_LISTENER = new NativeMouseListener() {
        @Override
        public void nativeMousePressed(NativeMouseEvent e) {
            // TODO -> check if this first click is even being propogated?
//            NativeMouseListener.super.nativeMouseClicked(nativeEvent); TODO use this
            switch (e.getButton()) {
                case NativeMouseEvent.BUTTON1 -> {
                    handleAutoclickerMouseHeldDown();
                }
            }
        }

        @Override
        public void nativeMouseReleased(NativeMouseEvent e) {
            switch (e.getButton()) {
                case NativeMouseEvent.BUTTON1 -> {
                    nullableCurrentHeldAutoclickerTask = null;
                }
            }
        }
    };

    @Nullable
    public static Thread nullableCurrentRandomDoubleClickTask = null;
    public static final NativeMouseListener RANDOM_DOUBLE_CLICK_LISTENER = new NativeMouseListener() {
        // TODO -> vs. nativeMouseClicked?
        @Override
        public void nativeMousePressed(NativeMouseEvent nativeEvent) {
            NativeMouseListener.super.nativeMousePressed(nativeEvent);
            if (nativeEvent.getButton() != NativeMouseEvent.BUTTON1) {
                return;
            }

            TODO; // random chance
            TODO; // maybe a delay will actually flag less
            MinecraftClient.getInstance().execute(() -> {
                ((MouseMixin) MINECRAFT_CLIENT_INSTANCE.mouse).invokeOnMouseButton(
                        MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(),
                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                        GLFW.GLFW_PRESS,
                        0);
            });
        }
    };

    public static void handleAutoclickerMouseHeldDown() {
        MinecraftClient clientDontUseThis = MinecraftClient.getInstance();
        // TODO -> method-ize that check
        if (nullableCurrentHeldAutoclickerTask == null && (clientDontUseThis.currentScreen == null || clientDontUseThis.currentScreen instanceof InventoryScreen) && clientDontUseThis.player instanceof ClientPlayerEntity player && !player.isUsingItem()) {
            isHeldAutoclickerPressed = true; // this is accounting for the initial mouse press, which is used
            final int[] currentAutoclickerMacroIndex = new int[]{RANDOM.nextInt(immutableRecordedAutoclickerClicks.length)};
            final boolean[] isCurrentlyReversed = new boolean[]{false};
            final float[] lerp = new float[]{autoclickerStartingMultiplier};
            nullableCurrentHeldAutoclickerTask = new Thread() {
                final HashSet<Integer> seenIndexes = new HashSet<>();
                int[] currentRecordedAutoclicker = immutableRecordedAutoclickerClicks[currentAutoclickerMacroIndex[0]];
                private final MinecraftClient threadClient = MinecraftClient.getInstance();
                final Runnable getNextRecordedAutoclicker = () -> {
                    int newIndex = RANDOM.nextInt(immutableRecordedAutoclickerClicks.length);
                    while (seenIndexes.contains(newIndex))
                        newIndex = RANDOM.nextInt(immutableRecordedAutoclickerClicks.length);
                    currentRecordedAutoclicker = immutableRecordedAutoclickerClicks[newIndex];
                    seenIndexes.add(newIndex);
                    if (seenIndexes.size() >= immutableRecordedAutoclickerClicks.length)
                        seenIndexes.clear();
                };

                @Override
                public void run() {
                    int currentAutoclickerIndex = 1; // this is accounting for the initial mouse press, which is used
                    int firstMacroLengthMinus1 = currentRecordedAutoclicker.length - 1;
                    while (nullableCurrentHeldAutoclickerTask == this) {
                        lerp[0] = firstMacroLengthMinus1 != -1
                                ? lerp(autoclickerStartingMultiplier, autoclickerEndingMultiplier, (float) currentAutoclickerIndex / firstMacroLengthMinus1)
                                : autoclickerEndingMultiplier;
                        LockSupport.parkNanos(
                                (long) (currentRecordedAutoclicker[currentAutoclickerIndex] / lerp[0]));
                        // TODO -> shift + right click could also be an autoclicker here
                        if (threadClient.currentScreen != null &&
                                (!isAutoClickInventoryEnabled || !(threadClient.currentScreen instanceof InventoryScreen && getIsKeyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)))) {
                            getNextRecordedAutoclicker.run();
                            currentAutoclickerIndex = 0; // TODO this seems retarded?
                            firstMacroLengthMinus1 = currentRecordedAutoclicker.length - 1; // TODO ?
                            isCurrentlyReversed[0] = false;
                        } // TODO -> change the speed if blocking/using item (?, not sure how to handle this exactly -> completely stopping clicks seems wrong, maybe immediately pivoting to the lower multiplier is better?)
                        else {
                            if (!isCurrentlyReversed[0]) {
                                if (currentAutoclickerIndex >= currentRecordedAutoclicker.length - 1) {
                                    firstMacroLengthMinus1 = -1;
                                    isCurrentlyReversed[0] = true;
                                    getNextRecordedAutoclicker.run();
                                    currentAutoclickerIndex = currentRecordedAutoclicker.length - 2; // this one ends on not pressed, so start on pressed
                                } // TODO -> external gui should give warnings for if you're wrapping around and suggest recording longer macros
                                else
                                    currentAutoclickerIndex++;
                            } else {
                                if (currentAutoclickerIndex <= 0) {
                                    isCurrentlyReversed[0] = false;
                                    getNextRecordedAutoclicker.run();
                                    currentAutoclickerIndex = 1; // this one ends on pressed, so start on not pressed
                                } else
                                    currentAutoclickerIndex--;
                            }
                            isHeldAutoclickerPressed = ((currentAutoclickerIndex - 1) & 1) == 0;
//                            if (isHeldAutoclickerPressed)
                            threadClient.execute(() -> {
//                                    if (MINECRAFT_CLIENT_INSTANCE.currentScreen == null && MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player && !player.isUsingItem()) {
//                                        KeyBindingMixin keyBindingMixin = (KeyBindingMixin) ATTACK_VANILLA;
//                                        keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
//                                    }
//                                    else if (MINECRAFT_CLIENT_INSTANCE.currentScreen instanceof InventoryScreen inventoryScreen) {
//                                        // TODO -> I should record the unclick timings for this
//                                    }
                                ((MouseMixin) MINECRAFT_CLIENT_INSTANCE.mouse).invokeOnMouseButton(
                                        MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(),
                                        GLFW.GLFW_MOUSE_BUTTON_LEFT,
                                        isHeldAutoclickerPressed ? GLFW.GLFW_PRESS : GLFW.GLFW_RELEASE,
                                        0);
                            });
                        }
                    }
                    isHeldAutoclickerPressed = false;
                    threadClient.execute(() -> ATTACK_VANILLA.setPressed(false));
                }
            };
            nullableCurrentHeldAutoclickerTask.start();
            Thread task = nullableCurrentHeldAutoclickerTask;
            new Thread() {
                MouseMovement[] currentRecordedAutoclicker = immutableRecordedAutoclickerMovements[currentAutoclickerMacroIndex[0]];
                int currentAutoclickerIndex = 1;
                int currentMacroIndexLengthMinus1 = currentRecordedAutoclicker.length - 1;
                int lastRecordedAutoclickerIndex = currentAutoclickerMacroIndex[0];
                private final MinecraftClient threadClient = MinecraftClient.getInstance();

                @Override
                public void run() {
                    try {
                        if (isAutoclickerShakeEnabled) {
                            MouseMovement mouseMovement = currentRecordedAutoclicker[0];
                            threadClient.execute(() -> { // TODO method-ize
                                long handle = MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle();
                                Mouse mouse = MINECRAFT_CLIENT_INSTANCE.mouse;
                                ((MouseMixin) mouse).invokeOnCursorPos(
                                        handle,
                                        mouse.getX() + mouseMovement.deltaX(),
                                        mouse.getY() + mouseMovement.deltaY());
                            });
                        }

                        while (nullableCurrentHeldAutoclickerTask == task) {
                            MouseMovement mouseMovement = currentRecordedAutoclicker[currentAutoclickerIndex];
                            LockSupport.parkNanos((long) (mouseMovement.delayNanos() / lerp[0]));
                            if (isAutoclickerShakeEnabled && threadClient.currentScreen == null && threadClient.player instanceof ClientPlayerEntity player && !player.isUsingItem())
                                threadClient.execute(() -> {
                                    long handle = MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle();
                                    Mouse mouse = MINECRAFT_CLIENT_INSTANCE.mouse;
                                    ((MouseMixin) mouse).invokeOnCursorPos(
                                            handle,
                                            mouse.getX() + mouseMovement.deltaX(),
                                            mouse.getY() + mouseMovement.deltaY());
                                });
//                            if (threadClient.currentScreen != null) {
//                                getNextRecordedAutoclicker.run();
//                                currentAutoclickerIndex = 0;
//                                firstMacroLengthMinus1 = currentRecordedAutoclicker.length - 1; // TODO ?
////                                isCurrentlyReversed = false;TODO remove
//                            } // TODO -> change the speed if blocking/using item (?, not sure how to handle this exactly -> completely stopping clicks seems wrong, maybe immediately pivoting to the lower multiplier is better?)
                            if (lastRecordedAutoclickerIndex != currentAutoclickerMacroIndex[0]) {
                                lastRecordedAutoclickerIndex = currentAutoclickerMacroIndex[0];
                                currentRecordedAutoclicker = immutableRecordedAutoclickerMovements[lastRecordedAutoclickerIndex];
                                currentAutoclickerIndex = 0;
                                currentMacroIndexLengthMinus1 = currentRecordedAutoclicker.length - 1;
                            } else if (!isCurrentlyReversed[0]) {
//                                if (currentAutoclickerIndex >= currentRecordedAutoclicker.length - 1) {
//                                    firstMacroLengthMinus1 = -1;
//                                    isCurrentlyReversed = true;
//                                    getNextRecordedAutoclicker.run();
//                                    currentAutoclickerIndex = currentRecordedAutoclicker.length - 2; // this one ends on not pressed, so start on pressed
//                                } // TODO -> external gui should give warnings for if you're wrapping around and suggest recording longer macros
//                                else
                                currentAutoclickerIndex++;
                            } else {
//                                if (currentAutoclickerIndex <= 0) {
//                                    isCurrentlyReversed = false;
//                                    getNextRecordedAutoclicker.run();
//                                    currentAutoclickerIndex = 1; // this one ends on pressed, so start on not pressed
//                                }
//                                else
                                currentAutoclickerIndex--;
                            }
//                            isHeldAutoclickerPressed = ((currentAutoclickerIndex - 1) & 1) == 0;
//                            if (isHeldAutoclickerPressed)
//                                threadClient.execute(() -> {
//                                    if (MINECRAFT_CLIENT_INSTANCE.currentScreen == null) {
//                                        KeyBindingMixin keyBindingMixin = (KeyBindingMixin) ATTACK_VANILLA;
//                                        keyBindingMixin.setTimesPressed(keyBindingMixin.getTimesPressed() + 1);
//                                    }
//                                });
                        }
//                        isHeldAutoclickerPressed = false;
//                        threadClient.execute(() -> ATTACK_VANILLA.setPressed(false));
                    } catch (Exception e) {
                        if (isDebugModeEnabled)
                            threadClient.execute(() -> MINECRAFT_CLIENT_INSTANCE.player.sendMessage(Text.literal(e.getMessage()), false));
                    }
                }
            }.start();
        }
    }
    // Cheats end
}