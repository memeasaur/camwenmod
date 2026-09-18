package com.example.overlay;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/** Standalone Windows native smoke test; does not launch Minecraft or show any windows. */
public class WindowsWaypointWindowSmoke {
    public interface CaptureQuery extends StdCallLibrary {
        CaptureQuery INSTANCE = Native.load("user32", CaptureQuery.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean GetWindowDisplayAffinity(WinDef.HWND window, IntByReference affinity);
    }

    public static void main(String[] args) throws Exception {
        User32 user = User32.INSTANCE;
        WinDef.HWND owner = user.CreateWindowEx(0, "STATIC", "Overlay test owner", WinUser.WS_POPUP,
                0, 0, 320, 200, null, null, null, null);
        if (owner == null) throw new AssertionError("Cannot create test owner");
        try {
            WindowsWaypointWindow overlay = new WindowsWaypointWindow(com.sun.jna.Pointer.nativeValue(owner.getPointer()));
            try {
                var field = WindowsWaypointWindow.class.getDeclaredField("window");
                field.setAccessible(true);
                WinDef.HWND window = (WinDef.HWND) field.get(overlay);
                IntByReference affinity = new IntByReference();
                if (!CaptureQuery.INSTANCE.GetWindowDisplayAffinity(window, affinity) || affinity.getValue() != 0x11)
                    throw new AssertionError("Capture exclusion is not active");
                int styles = user.GetWindowLong(window, WinUser.GWL_EXSTYLE);
                if ((styles & 0x080800A0) != 0x080800A0) throw new AssertionError("Missing click-through/no-activate styles");
                if (user.IsWindowVisible(window)) throw new AssertionError("Window shown before first protected frame");
                if (overlay.beginFrame() != null) throw new AssertionError("Unfocused owner must not render");
                for (int size : new int[]{64, 128, 32}) {
                    var frame = overlay.resize(size, size);
                    frame.setRGB(0, 0, 0x80FF0000);
                    overlay.uploadFrame();
                    if (user.IsWindowVisible(window)) throw new AssertionError("Upload must not show the window");
                    var pixels = WindowsWaypointWindow.class.getDeclaredField("pixels");
                    pixels.setAccessible(true);
                    int pixel = ((com.sun.jna.Pointer) pixels.get(overlay)).getInt(0);
                    if (pixel != 0x80800000) throw new AssertionError("DIB must contain premultiplied BGRA");
                }
                overlay.hide();
                overlay.close();
                if (user.IsWindow(window)) throw new AssertionError("Window was not destroyed");
                overlay.close(); // Idempotent cleanup.
            } finally { overlay.close(); }
        } finally { user.DestroyWindow(owner); }
        System.out.println("PASS: capture exclusion, input styles, visibility, focus gating, bitmap upload/resize, premultiplied alpha, and cleanup");
    }
}
