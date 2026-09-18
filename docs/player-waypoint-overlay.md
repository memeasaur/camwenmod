# Player waypoint overlay

Player waypoints now use a separate, transparent Windows window above Minecraft. Rally waypoints still use the original in-game HUD renderer. The existing ALL / ENEMIES / NONE setting and disable key continue to control player waypoints. Faces, team colors, distances, and aim-hover names and coordinates are preserved; text uses the system monospace font.

Use Windows 10 version 2004 or newer, or Windows 11, in windowed or borderless mode. Exclusive fullscreen can prevent desktop overlays from appearing. The overlay tracks the Minecraft client area and GUI scale, accepts no mouse input, does not activate, and hides when Minecraft loses focus, opens a menu, hides the HUD, or leaves the world. It is destroyed on client shutdown.

The window requests `WDA_EXCLUDEFROMCAPTURE` before it is shown. Player markers are never drawn into Minecraft's framebuffer, including when overlay initialization fails or on other operating systems. Failures are logged as `Player waypoints disabled: protected overlay unavailable`; restart after fixing the cause.

Windows capture exclusion is best effort, not a guarantee for every recording or screen-sharing application. See [Microsoft's SetWindowDisplayAffinity documentation](https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setwindowdisplayaffinity). Test the actual recording/share source before relying on it. Other in-game features are not moved by this change.

## Validation

- Build with Java 25 and Gradle 9.7.1 (`gradle build`).
- The standalone `tests/WindowsWaypointWindowSmoke.java` can be compiled with `WindowsWaypointWindow.java` and Minecraft's JNA / JNA platform jars on the classpath, then run as `com.example.overlay.WindowsWaypointWindowSmoke`. It creates only hidden test windows and checks capture affinity, input styles, focus gating, and destruction.
- In a world with another player, verify ALL / ENEMIES / NONE, both team colors, face/hat, distance, and aim-hover details; send rally coordinates and confirm the pink rally stays in-game.
- Compare the desktop against a Minecraft screenshot and the actual OBS/Discord window and display capture sources. Player markers should appear locally and be absent from supported captures; rallies should remain in both.
- Check F1, menus, alt-tab, minimize/restore, resize, GUI scale, monitors with different DPI, disconnect/reconnect, and client exit. Test windowed and borderless mode separately.

The native smoke test and build do not replace the multiplayer and recording checks above.
