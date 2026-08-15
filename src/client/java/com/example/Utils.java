package com.example;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.Configs.CheatConfig.isGuiCheatsPvpDisabling;
import static com.example.Configs.Config.*;
import static com.example.Constants.GSON;
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.*;
import static com.example.UntitledClient.isBackwardEnabled;


public class Utils {
    public static boolean getIsKeyPressed(int glfwKeybind) {
        return glfwKeybind != -1 && GLFW.glfwGetKey(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), glfwKeybind) == GLFW.GLFW_PRESS;
    }
    public static boolean getIsKeyBindingPressed(KeyBinding keyBinding) {
        TODO; // handle both mouse and keyboard here apparently
        return getIsKeyPressed(InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode());
    }

    public static void putNameplateUuidEntry(Map.Entry<UUID, String> entry) {
        nameplateUuids = Stream.concat(nameplateUuids.entrySet().stream(), Stream.of(entry))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
        handleNameplateSave();
    }
    public static void removeNameplateUuidEntry(UUID uuid) {
        nameplateUuids = nameplateUuids.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(uuid))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        handleNameplateSave();
    }
    private static void handleNameplateSave() {
        handleUnsafeJsonSave("nameplates", nameplateUuids);
    }
    public static void doMovementToggleDisable() {
        isSneakEnabled = false;
        isSprintEnabled = false;

        isJumpEnabled = false;
        isForwardEnabled = false;
        isLeftEnabled = false;
        isRightEnabled = false;
        isBackwardEnabled = false;
    }
    public static void handlePvpDamage() {
        if (isMovementTogglePvpDisabling)
            doMovementToggleDisable();

        // Cheats start
        if (isGuiCheatsPvpDisabling)
            currentXrayType = "";
        // Cheats end
    }
    public static <T> void serializeJsonBlocking(String fileNamePrefix, T jsonCompliantObject) {
        try (FileWriter writer = new FileWriter("pvputils-" + fileNamePrefix + ".json")) {
            GSON.toJson(jsonCompliantObject, writer);
        } catch (IOException e) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            if (minecraftClient.player instanceof ClientPlayerEntity player)
                minecraftClient.execute(() -> player.sendMessage(Text.literal("serialization failed"), false));
        }
    }
    public static Map getDeserializedJsonBlocking(String fileNamePrefix) {
        try (FileReader reader = new FileReader("pvputils-" + fileNamePrefix + ".json")) {
            return GSON.fromJson(reader, Map.class); // TODO -> use typeToken to get type safety here somehow (?)
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                if (minecraftClient.player instanceof ClientPlayerEntity player)
                    minecraftClient.execute(() -> player.sendMessage(Text.literal("deserialization failed: " + e.getMessage()), false));
                // TODO -> console this
            }
            return null;
        }
    }

    private static class saveTaskEntry {
        public AtomicBoolean atomicBoolean;
        public Boolean bool;
        public saveTaskEntry() {
            this.atomicBoolean = new AtomicBoolean(false);
            this.bool = false;
        }
    }
    private static final HashMap<String, saveTaskEntry> saveTaskData = new HashMap<>();
    public static void handleUnsafeJsonSave(String fileNamePrefix, Map map) {
        try {
            saveTaskData.putIfAbsent(fileNamePrefix, new saveTaskEntry());
            if (saveTaskData.get(fileNamePrefix).atomicBoolean.compareAndSet(false, true)) {
                var entry = saveTaskData.get(fileNamePrefix);
                new Thread(() -> {
                    entry.bool = true;
                    while (entry.bool) {
                        entry.bool = false;
                        serializeJsonBlocking(fileNamePrefix, map);
                    }
                    entry.atomicBoolean.set(false);
                }).start();
            }
            else
                saveTaskData.get(fileNamePrefix).bool = true; // TODO -> this can race condition (?)
        }
        catch (Exception e) {
            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                player.sendMessage(Text.literal("unsafejsonsave err: " + e.getMessage()), false);
        }
    }
}
