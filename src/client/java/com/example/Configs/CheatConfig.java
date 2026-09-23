package com.example.Configs;

public class CheatConfig {
    //    public boolean isEthylene = false;
    public boolean isTargetingMarginReverted = false; // TODO -> struct
    public float staticTargetingMarginBypass = .0f;
    public float movingTargetMarginBypass = 0.f;
    public float doubleMovingTargetMarginBypass = 0.f;
//    public float targetingMarginWidthBypass = 0.f;
//    public double attackVelocityBypass = 0.6;
//    public boolean isAutoCobweb = false; // TODO -> struct?
//    public double cobwebRangeBypassDelta = .0f;

    public float computeTargetingMarginBypass(
            boolean isMoving, boolean isTargetMovingPlayer) {
        float base = isTargetingMarginReverted
                ? .1f
                : 0.f;
        float one = staticTargetingMarginBypass;
        float two = isMoving
                ? movingTargetMarginBypass
                : 0.f;
        float three = isMoving && isTargetMovingPlayer
                ? doubleMovingTargetMarginBypass
                : 0.f;
        return base + Math.max(one, Math.max(two, three));
    }
}
