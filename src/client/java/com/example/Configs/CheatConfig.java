package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import static com.example.Configs.Utils.handleSave;

public class CheatConfig {
    public static boolean isGuiCheatsPvpDisabling = true;

    public static int[][] immutableRecordedAutoclickerClicks = new int[0][];
    public static MouseMovement[][] immutableRecordedAutoclickerMovements = new MouseMovement[0][];
    public static float autoclickerStartingMultiplier = 1.f;
    public static float autoclickerEndingMultiplier = .1f;
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

    TODO; // -> put a checkBox boolean here for this
//    public static int glfwToggleRandomDoubleClickKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public static int randomDoubleClickMaxInterval = 0; // TODO

    // TODO -> impl? might not be possible with how simulated clients get replicated
    public static int glfwToggleMirrorMovementKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public static void saveCheatConfig() {
        handleSave("cheat-config", CheatConfig.class);
    }
}
