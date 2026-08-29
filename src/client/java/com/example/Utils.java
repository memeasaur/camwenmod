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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.Constants.GSON;
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.*;
import static com.example.UntitledClient.isBackwardEnabled;


public class Utils {
    public static boolean getIsKeyPressed(int glfwKeybind) {
        return glfwKeybind != -1 && GLFW.glfwGetKey(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), glfwKeybind) == GLFW.GLFW_PRESS;
    }

    public static boolean getIsKeyBindingPressed(KeyBinding keyBinding) {
        InputUtil.Key key = InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey());
        if (key.getCategory() == InputUtil.Type.KEYSYM) {
            return getIsKeyPressed(InputUtil.fromTranslationKey(keyBinding.getBoundKeyTranslationKey()).getCode());
        } else if (key.getCategory() == InputUtil.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(MINECRAFT_CLIENT_INSTANCE.getWindow().getHandle(), key.getCode()) == GLFW.GLFW_PRESS;
        } else {
            Objects.requireNonNull(null);
            return false;
        }
    }

    public static void putNameplateUuidEntry(Map.Entry<UUID, String> entry) {
        config.nameplateUuids = Stream.concat(config.nameplateUuids.entrySet().stream(), Stream.of(entry))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
        config.saveConfig();
//        handleNameplateSave();
    }

    public static void removeNameplateUuidEntry(UUID uuid) {
        config.nameplateUuids = config.nameplateUuids.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(uuid))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        config.saveConfig();
//        handleNameplateSave();
    }

    //    private static void handleNameplateSave() {
//        handleUnsafeJsonSave("nameplates", nameplateUuids);
//    }
    public static void doMovementToggleDisable() {
        config.isSneakEnabled = false;
        config.isSprintEnabled = false;

        isJumpEnabled = false;
        isForwardEnabled = false;
        isLeftEnabled = false;
        isRightEnabled = false;
        isBackwardEnabled = false;
    }

    public static void onPvpDamage() {
        if (config.isMovementTogglePvpDisabling) {
            doMovementToggleDisable();
        }

        // Cheats start
        if (cheatConfig.isGuiCheatsPvpDisabling && currentXrayType != "") {
            currentXrayType = "";
            MINECRAFT_CLIENT_INSTANCE.worldRenderer.reload(); // TODO -> method-ize
        }
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

    public static <T> T getDeserializedJsonBlocking(String fileNamePrefix, Class<T> clazz) {
        try (FileReader reader = new FileReader("pvputils-" + fileNamePrefix + ".json")) {
            return GSON.fromJson(reader, clazz);
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

//    private static class saveTaskEntry {
//        public AtomicBoolean atomicBoolean;
//        public Boolean bool;
//        public saveTaskEntry() {
//            this.atomicBoolean = new AtomicBoolean(false);
//            this.bool = false;
//        }
//    }
//    private static final HashMap<String, saveTaskEntry> saveTaskData = new HashMap<>();
//    public static <T> void handleUnsafeJsonSave(String fileNamePrefix, T object) {
//        try {
//            saveTaskData.putIfAbsent(fileNamePrefix, new saveTaskEntry());
//            if (saveTaskData.get(fileNamePrefix).atomicBoolean.compareAndSet(false, true)) {
//                var entry = saveTaskData.get(fileNamePrefix);
//                new Thread(() -> {
//                    entry.bool = true;
//                    while (entry.bool) {
//                        entry.bool = false;
//                        serializeJsonBlocking(fileNamePrefix, object);
//                    }
//                    entry.atomicBoolean.set(false);
//                }).start();
//            }
//            else
//                saveTaskData.get(fileNamePrefix).bool = true; // TODO -> this can race condition (?)
//        }
//        catch (Exception e) {
//            if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
//                player.sendMessage(Text.literal("unsafejsonsave err: " + e.getMessage()), false);
//        }
}
