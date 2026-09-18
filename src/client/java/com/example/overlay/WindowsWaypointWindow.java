package com.example.overlay;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

// TODO -> AI made this
final class WindowsWaypointWindow implements AutoCloseable {
    public interface CaptureApi extends com.sun.jna.win32.StdCallLibrary {
        CaptureApi INSTANCE = Native.load("user32", CaptureApi.class, W32APIOptions.DEFAULT_OPTIONS);
        boolean IsIconic(WinDef.HWND window);
        boolean ClientToScreen(WinDef.HWND window, WinDef.POINT point);
        boolean SetWindowDisplayAffinity(WinDef.HWND window, int affinity);
    }

    private final User32 user = User32.INSTANCE;
    private final GDI32 gdi = GDI32.INSTANCE;
    private final WinDef.HWND owner;
    private WinDef.HWND window;
    private WinDef.HDC dc;
    private WinDef.HBITMAP bitmap;
    private WinNT.HANDLE previousBitmap;
    private Pointer pixels;
    private BufferedImage image;

    WindowsWaypointWindow(long ownerHandle) {
        owner = new WinDef.HWND(Pointer.createConstant(ownerHandle));
        // WS_EX_LAYERED | WS_EX_TRANSPARENT | WS_EX_NOACTIVATE | WS_EX_TOOLWINDOW
        window = user.CreateWindowEx(0x080800A0, "STATIC", "Player waypoints",
                WinUser.WS_POPUP, 0, 0, 1, 1, owner, null, null, null);
        if (window == null) throw new IllegalStateException("Cannot create waypoint window: " + Native.getLastError());
        // Apply before the first ShowWindow; never fall back to a captured window.
        try {
            if (!CaptureApi.INSTANCE.SetWindowDisplayAffinity(window, 0x11)) {
                throw new IllegalStateException("Cannot exclude waypoint window from capture");
            }
        } catch (RuntimeException | LinkageError error) {
            close();
            throw error;
        }
    }

    BufferedImage beginFrame() {
        if (!owner.equals(user.GetForegroundWindow()) || CaptureApi.INSTANCE.IsIconic(owner)) {
            hide();
            return null;
        }
        WinDef.RECT rect = new WinDef.RECT();
        if (!user.GetClientRect(owner, rect)) throw new IllegalStateException("Cannot read Minecraft bounds");
        int width = rect.right - rect.left, height = rect.bottom - rect.top;
        if (width <= 0 || height <= 0) { hide(); return null; }
        return resize(width, height);
    }

    BufferedImage resize(int width, int height) {
        if (image == null || image.getWidth() != width || image.getHeight() != height) {
            releaseBitmap();
            dc = gdi.CreateCompatibleDC(null);
            WinGDI.BITMAPINFO info = new WinGDI.BITMAPINFO();
            info.bmiHeader.biWidth = width;
            info.bmiHeader.biHeight = -height;
            info.bmiHeader.biPlanes = 1;
            info.bmiHeader.biBitCount = 32;
            info.bmiHeader.biCompression = WinGDI.BI_RGB;
            PointerByReference bits = new PointerByReference();
            bitmap = gdi.CreateDIBSection(dc, info, WinGDI.DIB_RGB_COLORS, bits, null, 0);
            if (dc == null || bitmap == null) throw new IllegalStateException("Cannot allocate waypoint bitmap");
            previousBitmap = gdi.SelectObject(dc, bitmap);
            pixels = bits.getValue();
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        }
        return image;
    }

    void present() {
        if (!owner.equals(user.GetForegroundWindow()) || CaptureApi.INSTANCE.IsIconic(owner)) { hide(); return; }
        uploadFrame();
        user.ShowWindow(window, WinUser.SW_SHOWNOACTIVATE);
        user.SetWindowPos(window, new WinDef.HWND(Pointer.createConstant(-1)), 0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE);
    }

    /** Upload while hidden as well, so display affinity is established before any pixels are visible. */
    void uploadFrame() {
        int[] data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        pixels.write(0, data, 0, data.length);
        WinDef.POINT position = new WinDef.POINT();
        if (!CaptureApi.INSTANCE.ClientToScreen(owner, position)) throw new IllegalStateException("Cannot position waypoint window");
        WinUser.SIZE size = new WinUser.SIZE();
        size.cx = image.getWidth();
        size.cy = image.getHeight();
        WinUser.BLENDFUNCTION blend = new WinUser.BLENDFUNCTION();
        blend.SourceConstantAlpha = (byte) 255;
        blend.AlphaFormat = WinUser.AC_SRC_ALPHA;
        if (!user.UpdateLayeredWindow(window, null, position, size, dc, new WinDef.POINT(), 0, blend, WinUser.ULW_ALPHA)) {
            throw new IllegalStateException("Cannot update waypoint window: " + Native.getLastError());
        }
    }

    void hide() { if (window != null) user.ShowWindow(window, WinUser.SW_HIDE); }

    private void releaseBitmap() {
        if (dc != null && previousBitmap != null) gdi.SelectObject(dc, previousBitmap);
        if (bitmap != null) gdi.DeleteObject(bitmap);
        if (dc != null) gdi.DeleteDC(dc);
        bitmap = null;
        previousBitmap = null;
        dc = null;
        image = null;
        pixels = null;
    }

    @Override public void close() {
        if (window != null) { user.DestroyWindow(window); window = null; }
        releaseBitmap();
    }
}
