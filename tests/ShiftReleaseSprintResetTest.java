// codex start
import com.example.ShiftReleaseSprintReset;

public class ShiftReleaseSprintResetTest {
    private static void expect(boolean expected, boolean actual) {
        if (expected != actual) throw new AssertionError("Expected " + expected + " but got " + actual);
    }

    public static void main(String[] args) {
        ShiftReleaseSprintReset reset = new ShiftReleaseSprintReset();
        expect(false, reset.update(true, true, false, true)); // No press, no release.
        expect(false, reset.update(true, true, true, true));
        expect(false, reset.update(true, true, true, true)); // Holding does not reset.
        expect(true, reset.update(true, true, false, true));
        expect(false, reset.update(true, true, false, true)); // Only one release edge.
        expect(false, reset.update(true, true, true, false));
        expect(false, reset.update(true, true, false, false)); // Already reset.
        expect(false, reset.update(false, true, true, true));
        expect(false, reset.update(true, true, false, true)); // Disabled press is not armed.
        expect(false, reset.update(true, true, true, true));
        expect(false, reset.update(true, false, false, true)); // A screen must not trigger a reset.
        expect(false, reset.update(true, true, false, true)); // No deferred release after screen closes.
        expect(false, reset.update(true, true, true, true));
        expect(false, reset.update(false, true, false, true)); // Disabled release.
        System.out.println("PASS: release edges, held key, reset state, disabled setting, and screen transitions");
    }
}
// codex end