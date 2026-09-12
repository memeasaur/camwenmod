package com.example.Configs;

import static com.example.UntitledClient.rageCheatLevel;

public class CheatConfig {
//    public boolean isAutomaticWTapping = true; // TODO -> impl?
    public boolean isEthylene = false;
    private float targetingMarginBypass = .0f;
    public float targetingMarginWidthBypass = 0.f;
//    public boolean isSneakyReachEnabled = false;
    public double attackVelocityBypass = 0.6;
    public boolean isAutoCobweb = false; // TODO -> struct?
    public double cobwebRangeBypassDelta = .5f;

    public void setTargetingMarginBypass(float value) {
        this.targetingMarginBypass = value;
    }
    public float getTargetingMarginBypass() {
        return this.targetingMarginBypass + rageCheatLevel.TargetingMarginBypassDelta;
    }
}
