package com.example.Screens;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.example.Constants.*;
import static com.example.Screens.Constants.*;
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

//    static Screen getAbstractKeybindInputScreen(
//            Text title, Consumer<Integer> consumer) {
//        return getAbstractInputScreen(title, (threadInstance) ->
//                consumer.accept(getGlfwInputBlocking(threadInstance, title)), Constants.CHEAT_CONFIG);
//    }

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
            } catch (Exception e) {
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                        player.sendMessage(Text.literal("getabstractkeyboardsequencescreen " + e.getMessage()), false);
                });
            }
        }, returnScreen);
    }

    // TODO -> use text input for this
    private static final boolean[] getFloatInputScreenFlag = new boolean[]{false};
    static Screen getDoubleInputScreen(
            Text title, Consumer<Double> consumer) {
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
                client.execute(() -> client.setScreen(CONFIG));
                // TODO -> going back to config twice seems odd here
            } else
                client.execute(() -> {
                    if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                        player.sendMessage(Text.literal("invalid float"), true); // TODO -> console
                });
        }, CONFIG);
    }
}
