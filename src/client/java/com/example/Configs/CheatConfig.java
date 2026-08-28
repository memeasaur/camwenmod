package com.example.Configs;

import com.example.MouseMovement;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

import static com.example.Utils.serializeJsonBlocking;

public class CheatConfig {
    public boolean isGuiCheatsPvpDisabling = true;

    public record ClickRecording(int[] clicks, MouseMovement[] movements, boolean isSlow) {
    }
    public ArrayList<ClickRecording> recordedClickSequences = new ArrayList<>();
    public float autoclickerJitterStartingMultiplier = 1.f;
    public float autoclickerJitterEndingMultiplier = 1.f;
    public float autoclickerSlowStartingMultiplier = 1.5f;
    public float autoclickerSlowEndingMultiplier = 1.5f;
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

    public boolean isAutoClickInventoryEnabled = true; // TODO -> finish

    // TODO -> triple-clicks possible?
    // TODO -> min interval?
    public boolean isRandomDoubleClickEnabled = true;
    public int randomDoubleClickMaxInterval = 0; // TODO

    // TODO -> should have an option for it making a noise if a player shows up nearby
    public boolean isPlayerWaypointsEnabled = false;

    public boolean isSneakyReachEnabled = false;

    public double attackVelocityBypass = 0.6;

    // TODO -> config
    public boolean isGrappleGroundCheckEnabled = false;

    // TODO -> impl? might not be possible with how simulated clients get replicated
//    public int glfwToggleMirrorMovementKeybind = GLFW.GLFW_KEY_UNKNOWN;

    public void saveCheatConfig() {
        serializeJsonBlocking("cheat-config", this);
    }
}
