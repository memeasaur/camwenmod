package com.example;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Random;
import java.util.concurrent.*;

public class Constants {
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new Gson();
    public static final MinecraftClient MINECRAFT_CLIENT_INSTANCE = MinecraftClient.getInstance();
    public static final byte FLY_BOOST_MULTIPLIER = 4;
    public static final int
            AQUA_RGB = TextColor.fromFormatting(Formatting.AQUA).getRgb(), // TODO ?
            RED_RGB = TextColor.fromFormatting(Formatting.RED).getRgb(),
            LIGHT_PURPLE_RGB = TextColor.fromFormatting(Formatting.LIGHT_PURPLE).getRgb(),
            YELLOW_RGB = TextColor.fromFormatting(Formatting.YELLOW).getRgb();
}