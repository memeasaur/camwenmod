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
        float one = isTargetingMarginReverted ? .1f : 0.f;
        float two = isMoving ? Math.max(movingTargetMarginBypass, staticTargetingMarginBypass) : staticTargetingMarginBypass;
        TODO;
        return one + two;
    }
}
