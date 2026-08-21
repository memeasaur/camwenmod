package com.example.Configs;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.Constants.MINECRAFT_CLIENT_INSTANCE;
import static com.example.Utils.*;

public class Utils {
    public static void init(String fileNamePrefix, Class<?> clazz) {
        TODO;
        if (getDeserializedJsonBlocking(fileNamePrefix, clazz) instanceof Map config) {
            for (Field field : clazz.getDeclaredFields()) {
//                    if ( instanceof Object object) { // TODO -> this doesn't handle the type changing
//                    }
                try {
                    field.set(null, config.get(field.getName()));
                } catch (Exception e) {
                    MinecraftClient minecraftClient = MinecraftClient.getInstance();
                    if (minecraftClient.player instanceof ClientPlayerEntity player)
                        minecraftClient.execute(() -> player.sendMessage(Text.literal("deserialize reflection err"), false));
                }
            }
        }
    }

    static void handleSave(String fileNamePrefix, Class<?> clazz) {
        handleUnsafeJsonSave(fileNamePrefix, Arrays.stream(clazz.getDeclaredFields()).map(field -> {
                    try {
                        return Map.entry(field.getName(), field.get(null));
                    } catch (IllegalAccessException e) {
                        if (MINECRAFT_CLIENT_INSTANCE.player instanceof ClientPlayerEntity player)
                            player.sendMessage(Text.literal("serialization reflection err"), false);
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}
