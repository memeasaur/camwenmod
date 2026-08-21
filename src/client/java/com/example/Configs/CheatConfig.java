package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import static com.example.Configs.Utils.handleSave;
import static com.example.Configs.Utils.init;

public class CheatConfig {
    public static boolean isGuiCheatsPvpDisabling = true;

    public static int[][] immutableRecordedAutoclickerClicks = new int[0][];
    public static MouseMovement[][] immutableRecordedAutoclickerMovements = new MouseMovement[0][];
    public static float autoclickerStartingMultiplier = 1.f;
    public static float autoclickerEndingMultiplier = 1.f;
    public static int glfwToggleAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static int glfwEnableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static int glfwDisableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static boolean isAutoclickerShakeEnabled = true;

    public static int glfwToggleBlockXrayKeybind = GLFW.GLFW_KEY_UNKNOWN; // TODO -> hold/active/disable
    public static int glfwTogglePlayerXrayKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public static boolean isAutomaticWTapping = true; // TODO -> impl?
    public static boolean isEthylene = false;
    public static boolean isAutoCobweb = false;

    public static float targetingMarginBypass = .0f;

    public static boolean isDarknessDisabled = false;

    public static boolean isAutoClickInventoryEnabled = false; // TODO -> finish

    // TODO -> triple-clicks possible?
    // TODO -> min interval?
    public static boolean isRandomDoubleClickEnabled = true;
    public static int randomDoubleClickMaxInterval = 0; // TODO

    public static boolean isPlayerWaypointsEnabled = false;

    // TODO -> impl? might not be possible with how simulated clients get replicated
    public static int glfwToggleMirrorMovementKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public static void saveCheatConfig() {
        handleSave("cheat-config", CheatConfig.class);
    }
}
