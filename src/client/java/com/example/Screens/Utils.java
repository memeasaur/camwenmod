package com.example.Screens;

import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import static com.example.Constants.*;
import static com.example.Screens.Constants.*;
import static com.example.Utils.getIsKeyPressed;

public class Utils {
    static Screen getAbstractInputScreen(Component title, Consumer<Minecraft> synchronousRunnable, Screen returnScreen) {
        return new Screen(title) {
            @Override
            protected void init() {
                getFloatInputScreenFlag[0] = false;
                new Thread(() -> {
                    Minecraft threadClientInstance = Minecraft.getInstance();
                    synchronousRunnable.accept(threadClientInstance);
                    threadClientInstance
                            .execute(() -> MINECRAFT_CLIENT_INSTANCE.setScreen(returnScreen));
                }).start();
            }
        };
    }

    static int getGlfwInputBlocking(Minecraft threadClientInstance, Component title) {
        try {
            int[] resultKey = new int[]{0};
            while (resultKey[0] == 0 && threadClientInstance.screen instanceof Screen screen && screen.getTitle().equals(title)) {
                CountDownLatch latch = new CountDownLatch(1);
                threadClientInstance.execute(() -> {
                    for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++)
                        if (GLFW.glfwGetKey(MINECRAFT_CLIENT_INSTANCE.getWindow().handle(), key) == GLFW.GLFW_PRESS) {
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
                if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)
                    player.displayClientMessage(Component.literal("getglfwinputblocking " + e.getMessage()), false);
            });
            throw new RuntimeException(e);
        }
    }

    static Screen getAbstractKeyboardSequenceScreen(Component title, Function<String, Boolean> isValidHandler, BiConsumer<String, Minecraft> finalStringConsumer, Screen returnScreen) {
        return getAbstractInputScreen(title, client -> {
            try {
                StringBuilder floatBuilder = new StringBuilder();
                HashSet<Integer> pressedKeys = new HashSet<>();
                while (client.screen instanceof Screen screen && screen.getTitle().equals(title)) {
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
            } catch (Exception e) {
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)
                        player.displayClientMessage(Component.literal("getabstractkeyboardsequencescreen " + e.getMessage()), false);
                });
            }
        }, returnScreen);
    }

    // TODO -> use text input for this
    private static final boolean[] getFloatInputScreenFlag = new boolean[]{false};
    static Screen getDoubleInputScreen(
            Component title, Consumer<Double> consumer) {
        return getAbstractKeyboardSequenceScreen(title, (string) -> {
            if (Character.isDigit(string.charAt(0))) {
                return true;
                }
            else if (string.charAt(0) == '.' && !getFloatInputScreenFlag[0]) {
                getFloatInputScreenFlag[0] = true;
                return true;
            } else
                return false;
        }, (finalFloatString, client) -> {
            if (!finalFloatString.isEmpty() && !finalFloatString.equals(".")) {
                consumer.accept(Double.parseDouble(finalFloatString));
                client.execute(() -> client.setScreen(buildConfig()));
                // TODO -> going back to config twice seems odd here
            } else
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof LocalPlayer player)
                        player.displayClientMessage(Component.literal("invalid float"), true); // TODO -> console
                });
        }, buildConfig());
    }
}
