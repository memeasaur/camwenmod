package com.example;

import com.example.Configs.CheatConfig;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import static com.example.Constants.GSON;
import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.UntitledClient.*;
import static com.example.UntitledClient.isBackwardEnabled;


public class Utils {
    public static boolean getIsKeyPressed(int glfwKeybind) {
        return glfwKeybind != -1 && GLFW.glfwGetKey(MINECRAFT_CLIENT_INSTANCE.getWindow().handle(), glfwKeybind) == GLFW.GLFW_PRESS;
    }

    public static boolean getIsKeyBindingPressed(KeyMapping keyBinding) {
        InputConstants.Key key = InputConstants.getKey(keyBinding.saveString());
        if (key.getType() == InputConstants.Type.KEYSYM) {
            return getIsKeyPressed(InputConstants.getKey(keyBinding.saveString()).getValue());
        } else if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(MINECRAFT_CLIENT_INSTANCE.getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
        } else {
            Objects.requireNonNull(null);
            return false;
        }
    }

//    public static void putNameplateUuidEntry(Map.Entry<UUID, String> entry) {
//        config.nameplateUuids = Stream.concat(config.nameplateUuids.entrySet().stream(), Stream.of(entry))
//                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue));
//        config.saveConfig();
////        handleNameplateSave();
//    }

//    public static void removeNameplateUuidEntry(UUID uuid) {
//        config.nameplateUuids = config.nameplateUuids.entrySet().stream()
//                .filter(entry -> !entry.getKey().equals(uuid))
//                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
//        config.saveConfig();

    /// /        handleNameplateSave();
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
        if (config.isGuiCheatsPvpDisabling && !Objects.equals(currentXrayType, "")) {
            currentXrayType = "";
            MINECRAFT_CLIENT_INSTANCE.levelRenderer.allChanged(); // TODO -> method-ize
        }
        // Cheats end
    }

    public static <T> void serializeJsonBlocking(String fileNamePrefix, T jsonCompliantObject) {
        try (FileWriter writer = new FileWriter("pvputils-" + fileNamePrefix + ".json")) {
            GSON.toJson(jsonCompliantObject, writer);
        } catch (IOException e) {
            Minecraft minecraftClient = Minecraft.getInstance();
            if (minecraftClient.player instanceof LocalPlayer player)
                minecraftClient.execute(() -> player.displayClientMessage(Component.literal("serialization failed"), false));
        }
    }

    public static <T> T getDeserializedJsonBlocking(String fileNamePrefix, Type clazz) {
        try (FileReader reader = new FileReader("pvputils-" + fileNamePrefix + ".json")) {
            return GSON.fromJson(reader, clazz);
        } catch (IOException e) {
            if (!(e instanceof FileNotFoundException)) {
                Minecraft minecraftClient = Minecraft.getInstance();
                if (minecraftClient.player instanceof LocalPlayer player)
                    minecraftClient.execute(() -> player.displayClientMessage(Component.literal("deserialization failed: " + e.getMessage()), false));
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

    public static Screen buildConfigScreen(
            String name, List<AbstractWidget> clickableWidgets) {
        return new Screen(Component.literal(name)) {
            @Override
            protected void init() {
                int y = 20;
                int x = 20;
                for (int i = 0; i < clickableWidgets.size(); ++i) {
                    int column = i % 4;
                    int row = i / 4;
                    AbstractWidget widget = clickableWidgets.get(i);
                    widget.setPosition(x + 150 * column, y + 20 * row);
                    addRenderableWidget(widget);
                }
            }
        };
    }

    private static String computeServerName() {
        if (MINECRAFT_CLIENT_INSTANCE.getCurrentServer() instanceof ServerData serverInfo) {
            return serverInfo.ip;
        }
        return "singlePlayer";
    }

    public static CheatConfig computeCheatConfig() {
        return cheatConfigs.computeIfAbsent(computeServerName(), v -> new CheatConfig());
    }

    public static ItemStack buildReplacementTeamLeatherItemStack(
            ItemStack original, Item replacement, int color) {
        ItemStack replacementStack = replacement.getDefaultInstance();
//        replacementStack.applyComponentsFrom(original.getComponents());
        replacementStack.set(
                DataComponents.DYED_COLOR,
                new DyedItemColor(color, true)
        );
        replacementStack.set(
                DataComponents.ENCHANTMENTS,
                original.getEnchantments()
        );
        return replacementStack;
    }
}
