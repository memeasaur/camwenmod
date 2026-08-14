package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import static com.example.Configs.Utils.handleSave;

public class CheatConfig {
    public static boolean isGuiCheatsPvpDisabling = true;

    public static int[][] immutableRecordedAutoclickerClicks = new int[0][];
    public static MouseMovement[][] immutableRecordedAutoclickerMovements = new MouseMovement[0][];
    public static float autoclickerStartingMultiplier = 1.f;
    public static float autoclickerEndingMultiplier = .95f;
    public static int glfwToggleAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static int glfwEnableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static int glfwDisableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public static int glfwToggleBlockXrayKeybind = GLFW.GLFW_KEY_UNKNOWN; // TODO -> hold/active/disable
    public static int glfwTogglePlayerXrayKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public static boolean isAutomaticWTapping = true;
    public static boolean isEthylene = true;
    public static boolean isCobwebAutoclicker = false;

    public static float targetingMarginBypass = .1f;

    public static void saveCheatConfig() {
        handleSave("cheat-config", CheatConfig.class);
    }
}
