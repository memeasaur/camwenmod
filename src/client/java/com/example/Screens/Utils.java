package com.example.Screens;

import com.example.Configs.CheatConfig;
import com.example.MouseMovement;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseMotionListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.example.Constants.*;
import static com.example.Constants.SCHEDULED_EXECUTOR_SERVICE;
import static com.example.Screens.Constants.*;
import static com.example.UntitledClient.cheatConfig;
import static com.example.Utils.getIsKeyPressed;

public class Utils {
    static Screen getAbstractInputScreen(Text title, Consumer<MinecraftClient> synchronousRunnable, Screen returnScreen) {
        return new Screen(title) {
            @Override
            protected void init() {
                getFloatInputScreenFlag[0] = false;
                new Thread(() -> {
                    MinecraftClient threadClientInstance = MinecraftClient.getInstance();
                    synchronousRunnable.accept(threadClientInstance);
                    threadClientInstance
                            .execute(() -> MINECRAFT_CLIENT_INSTANCE.setScreen(returnScreen));
                }).start();
            }
        };
    }
    static Screen getAbstractKeybindInputScreen(
            Text title, Consumer<Integer> consumer, Screen returnScreen) {
        return getAbstractInputScreen(title, (threadInstance) ->
                consumer.accept(getGlfwInputBlocking(threadInstance, title)), returnScreen);
    }
    static int getGlfwInputBlocking(MinecraftClient threadClientInstance, Text title) {
        try {
            int[] resultKey = new int[]{0};
            while (resultKey[0] == 0 && threadClientInstance.currentScreen instanceof Screen screen && screen.getTitle().equals(title)) {
                CountDownLatch latch = new CountDownLatch(1);
                threadClientInstance.execute(() -> {
                    for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++)
                        if (GLFW.glfwGetKey(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), key) == GLFW.GLFW_PRESS) {
                            resultKey[0] = key;
                            break;
                        }
                    latch.countDown();
                });
                latch.await();
            }
            return resultKey[0] != 0 && resultKey[0] != GLFW.GLFW_KEY_ESCAPE // TODO -> just make escape the zero value (?)
                    ? resultKey[0]
                    : GLFW.GLFW_KEY_UNKNOWN;
        } catch (Exception e) {
            threadClientInstance.execute(() -> {
                if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                    player.sendMessage(Text.literal("getglfwinputblocking " + e.getMessage()), false);
            });
            throw new RuntimeException(e);
        }
    }
    static Screen getAbstractKeyboardSequenceScreen(Text title, Function<String, Boolean> isValidHandler, BiConsumer<String, MinecraftClient> finalStringConsumer, Screen returnScreen) {
        return getAbstractInputScreen(title, client -> {
            try {
                StringBuilder floatBuilder = new StringBuilder();
                HashSet<Integer> pressedKeys = new HashSet<>();
                while (client.currentScreen instanceof Screen screen && screen.getTitle().equals(title)) {
                    CountDownLatch latch = new CountDownLatch(1);
                    int glfwKey = getGlfwInputBlocking(client, title);
                    client.execute(() -> {
                        if (GLFW.glfwGetKeyName(glfwKey, 0) instanceof String string && string.length() == 1 && isValidHandler.apply(string) && !pressedKeys.contains(glfwKey)) {
                            floatBuilder.append(string.charAt(0));
                            pressedKeys.add(glfwKey);
                            // TODO -> backspace support
                        }
                        pressedKeys.removeIf(integer -> !getIsKeyPressed(integer));
                        latch.countDown();
                    });
                    latch.await();
                }
                finalStringConsumer.accept(floatBuilder.toString(), client);
            }
            catch (Exception e) {
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                        player.sendMessage(Text.literal("getabstractkeyboardsequencescreen " + e.getMessage()), false);
                });
            }
        }, returnScreen);
    }
    private static final boolean[] getFloatInputScreenFlag = new boolean[]{false};
    static Screen getFloatInputScreen(Text title, Consumer<Float> consumer, Screen returnScreen) {
        return getAbstractKeyboardSequenceScreen(title, (string) -> {
            if (Character.isDigit(string.charAt(0)))
                return true;
            else if (string.charAt(0) == '.' && !getFloatInputScreenFlag[0]) {
                getFloatInputScreenFlag[0] = true;
                return true;
            }
            else
                return false;
        }, (finalFloatString, client) -> {
            if (!finalFloatString.isEmpty() && !finalFloatString.equals(".")) {
                consumer.accept(Float.parseFloat(finalFloatString));
                client.execute(() ->
                        client.setScreen(CONFIG));
                // TODO -> going back to config twice seems odd here
            }
            else
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                        player.sendMessage(Text.literal("invalid float"), true); // TODO -> console
                });
        }, returnScreen);
    }
    static CompletableFuture<HttpResponse<String>> getHandledMojangApiFuture(Supplier<CompletableFuture<HttpResponse<String>>> unhandledFutureSupplier) {
        return unhandledFutureSupplier.get()
                .exceptionally(err -> {
                    MinecraftClient.getInstance().execute(() -> {
                        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                            player.sendMessage(Text.literal("(nameplate updater) err: " + err.getMessage() + " -> waiting 30s"), false);
                    });
                    return null;
                })
                .thenCompose(response -> {
                    if (response == null) {
                        CompletableFuture<HttpResponse<String>> future = new CompletableFuture<>();
                        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> getHandledMojangApiFuture(unhandledFutureSupplier) // TODO -> im pretty sure handle the http exceptions the same way
                                .thenAccept(future::complete), 30, TimeUnit.SECONDS);
                        return future;
                    } else if (response.statusCode() != 200) {
                        MinecraftClient.getInstance().execute(() -> {
                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                                player.sendMessage(Text.literal("(nameplate updater) mojang api rate limited -> waiting 30s"), false);
                        });
                        CompletableFuture<HttpResponse<String>> future = new CompletableFuture<>();
                        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> getHandledMojangApiFuture(unhandledFutureSupplier) // TODO -> im pretty sure handle the http exceptions the same way
                                .thenAccept(future::complete), 30, TimeUnit.SECONDS);
                        return future;
                    } else
                        return CompletableFuture.completedFuture(response);
                });
    }
    static void handleAbstractMojangApiNameplateUpdaterScreen(Function<ClientPlayNetworkHandler, ArrayList<CompletableFuture<Constants.nameplateUpdaterEntry>>> handler) {
        try {
            if (MINECRAFT_CLIENT_INSTANCE.getNetworkHandler() instanceof ClientPlayNetworkHandler networkHandler) {
                ArrayList<CompletableFuture<Constants.nameplateUpdaterEntry>> futureEntries = handler.apply(networkHandler);
                CompletableFuture.allOf(futureEntries.toArray(CompletableFuture[]::new))
                        .thenRun(() -> {
                            currentNameplateUpdatePlayers = futureEntries.stream()
                                    .map(CompletableFuture::join)
                                    .filter(Objects::nonNull) // TODO -> current solution adds completableFuture(null)s when it probably doesn't have to
                                    .toArray(Constants.nameplateUpdaterEntry[]::new);
                            MinecraftClient.getInstance().execute(() ->
                                    MINECRAFT_CLIENT_INSTANCE.setScreen(NAMEPLATE_UPDATER));
                        });
            }
        }
        catch (Exception e) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                player.sendMessage(Text.literal(e.getMessage()), false);
        }
    }

    // Cheats start
    static Text getAutoclickerText(int[] macro) {
        double durationSeconds = Arrays.stream(macro)
                .mapToLong(i -> i)
                .sum() / 1_000_000_000.0;
        int totalClicks = macro.length / 2;
        return Text.of(totalClicks + " clicks recorded over ~" + durationSeconds + "s. cps: " + (totalClicks / durationSeconds));
    }
    // Cheats end

    private static final Object AUTOCLICK_MACRO_RECORDER_LOCK = new Object();
    public static Screen BuildAutoclickMacroRecorderScreen(final Text title, final boolean isSlow) {
        return getAbstractInputScreen(
                title,
                (threadClientInstance) -> {
                    try {
                        ArrayList<Integer> mutableMacroClicks = new ArrayList<>();
                        long[] lastClickEventNanoseconds = new long[]{System.nanoTime()};
                        BiConsumer<long[], ArrayList<Integer>> handleAutoclickMacroRecorderIteration = (nanoseconds, macroList) -> {
                            long currentNanoseconds = System.nanoTime();
                            long delay = currentNanoseconds - lastClickEventNanoseconds[0];
                            if (delay > Integer.MAX_VALUE ||
                                    (!(threadClientInstance.currentScreen instanceof Screen screen) || !screen.getTitle().equals(title)))
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
                        boolean flag = !mutableMacroClicks.isEmpty();
                        if (flag) {
//                        int oldLength = cheatConfig.recordedClickSequences.length;
//                        {
//                            int newLength = oldLength + 1;
//                            {
//                                CheatConfig.clickRecording[] newMatrix = new CheatConfig.clickRecording[newLength];
//                                System.arraycopy(cheatConfig.recordedClickSequences, 0, newMatrix, 0, oldLength);
//                                cheatConfig.recordedClickSequences = newMatrix;
//                            }
//                            {
//                                MouseMovement[][] newMatrix1 = new MouseMovement[newLength][];
//                                System.arraycopy(cheatConfig.recordedClickSequences, 0, newMatrix1, 0, oldLength);
//                                cheatConfig.recordedClickSequences = newMatrix1;
//                            }
//                        }
                            int[] newRecordedAutoclickerClicks = mutableMacroClicks.stream().skip(2).mapToInt(Integer::intValue).toArray();
                            MouseMovement[] newRecordedAutoclickerMovements = mutableMacroMovements.toArray(new MouseMovement[0]);
                            cheatConfig.recordedClickSequences.add(new CheatConfig.ClickRecording(
                                    newRecordedAutoclickerClicks,
                                    newRecordedAutoclickerMovements,
                                    isSlow));
                        }
                        threadClientInstance.execute(() -> {
                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player) {
                                player.sendMessage(flag
                                        ? getAutoclickerText(cheatConfig.recordedClickSequences.getLast().clicks())
                                        : Text.literal("invalid macro"), true); // TODO -> console
                            }
                        });
                    } catch (Exception e) {
                        threadClientInstance.execute(() -> {
                            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                                player.sendMessage(Text.literal(e.getMessage()), false);
                        });
                    }
                },
                CHEAT_CONFIG);
    }
}
