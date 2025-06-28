package com.example;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.net.http.HttpClient;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
}
