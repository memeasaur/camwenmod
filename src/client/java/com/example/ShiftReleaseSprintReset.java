// codex start
package com.example;
public final class ShiftReleaseSprintReset {
    private boolean wasPressed;

    public boolean update(boolean enabled, boolean movementAllowed, boolean pressed, boolean needsReset) {
        boolean release = wasPressed && !pressed;
        wasPressed = enabled && movementAllowed && pressed;
        return enabled && movementAllowed && release && needsReset;
    }
}
// codex end