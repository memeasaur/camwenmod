package com.example.Configs;

import static com.example.UntitledClient.rageCheatLevel;

public class CheatConfig {
    public boolean isEthylene = false;
    private float staticTargetingMarginBypass = .0f;
    public float movingTargetMarginBypass = 0.f;
//    public float targetingMarginWidthBypass = 0.f;
    public double attackVelocityBypass = 0.6;
    public boolean isAutoCobweb = false; // TODO -> struct?
    public double cobwebRangeBypassDelta = .5f;

    public void setStaticTargetingMarginBypass(float value) {
        this.staticTargetingMarginBypass = value;
    }
    public float getStaticTargetingMarginBypass() {
        return this.staticTargetingMarginBypass + rageCheatLevel.TargetingMarginBypassDelta;
    }
}
