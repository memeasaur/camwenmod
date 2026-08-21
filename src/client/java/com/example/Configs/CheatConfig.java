package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import static com.example.Utils.serializeJsonBlocking;

public class CheatConfig {
    public boolean isGuiCheatsPvpDisabling = true;

    public int[][] immutableRecordedAutoclickerClicks = new int[0][];
    public MouseMovement[][] immutableRecordedAutoclickerMovements = new MouseMovement[0][];
    public float autoclickerStartingMultiplier = 1.f;
    public float autoclickerEndingMultiplier = 1.f;
    public int glfwToggleAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public int glfwEnableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public int glfwDisableAutoclickerKeybind = GLFW.GLFW_KEY_UNKNOWN;
    public boolean isAutoclickerShakeEnabled = true;

    public int glfwToggleBlockXrayKeybind = GLFW.GLFW_KEY_UNKNOWN; // TODO -> hold/active/disable
    public int glfwTogglePlayerXrayKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public boolean isAutomaticWTapping = true; // TODO -> impl?
    public boolean isEthylene = true;
    public boolean isAutoCobweb = false;

    public float targetingMarginBypass = .1f;

    public boolean isDarknessDisabled = true;

    public boolean isAutoClickInventoryEnabled = false; // TODO -> finish

    // TODO -> triple-clicks possible?
    // TODO -> min interval?
    public boolean isRandomDoubleClickEnabled = true;
    public int randomDoubleClickMaxInterval = 0; // TODO

    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;

    // TODO -> impl? might not be possible with how simulated clients get replicated
    public int glfwToggleMirrorMovementKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public void saveCheatConfig() {
        serializeJsonBlocking("cheat-config", this);
//        handleSave("cheat-config", CheatConfig.class);
    }
}
