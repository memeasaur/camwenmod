package com.example.Configs;

import static com.example.UntitledClient.rageCheatLevel;

public class CheatConfig {
    public boolean isEthylene = false;
    public boolean isTargetingMarginReverted = false; // TODO -> struct
    public float staticTargetingMarginBypass = .0f;
    public float movingTargetMarginBypass = 0.f;
//    public float targetingMarginWidthBypass = 0.f;
//    public double attackVelocityBypass = 0.6;
//    public boolean isAutoCobweb = false; // TODO -> struct?
    public double cobwebRangeBypassDelta = .5f;

    public float computeTargetingMarginBypass(boolean isMoving) {
        float one = isTargetingMarginReverted ? .1f : 0.f;
        float two = isMoving ? Math.max(movingTargetMarginBypass, staticTargetingMarginBypass) : staticTargetingMarginBypass;
        return one + two + rageCheatLevel.TargetingMarginBypassDelta;
    }
}
