package com.example.overlayTodoAi;

import com.example.Configs.Config;
import com.example.Configs.CheatConfig;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.example.UntitledClient.cheatConfigs;
import static com.example.UntitledClient.config;
import static com.example.Utils.computeCheatConfig;
import static com.example.Utils.serializeJsonBlocking;

// codex start
/** Interactive configuration in a capture-excluded native window outside Minecraft's framebuffer. */
public final class ExternalConfigWindow {
    private static final int WDA_EXCLUDEFROMCAPTURE = 0x11;
    private static final int GWLP_HWNDPARENT = -8;
    private static JFrame frame;

    private ExternalConfigWindow() {
    }

    public static void show() {
        toggle();
    }

    public static void toggle() {
        Minecraft client = Minecraft.getInstance();
        if (!Platform.isWindows()) {
            reportFailure(client, new UnsupportedOperationException("external config requires Windows"));
            return;
        }

        long minecraftHandle = GLFWNativeWin32.glfwGetWin32Window(client.getWindow().handle());
        int minecraftX = client.getWindow().getX();
        int minecraftY = client.getWindow().getY();
        int minecraftWidth = client.getWindow().getWidth();
        int minecraftHeight = client.getWindow().getHeight();
        CheatConfig activeCheatConfig = computeCheatConfig();
        SwingUtilities.invokeLater(() -> {
            if (frame != null && frame.isDisplayable()) {
//                frame.setVisible(true);
//                frame.toFront();
//                frame.requestFocus();
                closeOnEventThread();
                return;
            }
            try {
                frame = createFrame(activeCheatConfig);
                frame.pack();
                frame.setLocation(
                        minecraftX + Math.max(0, (minecraftWidth - frame.getWidth()) / 2),
                        minecraftY + Math.max(0, (minecraftHeight - frame.getHeight()) / 2));

                // Create the native peer while hidden so capture exclusion is set before first display.
                frame.addNotify();
                WinDef.HWND configHandle = new WinDef.HWND(Native.getWindowPointer(frame));
                WinDef.HWND ownerHandle = new WinDef.HWND(Pointer.createConstant(minecraftHandle));
                User32.INSTANCE.SetWindowLongPtr(configHandle, GWLP_HWNDPARENT, ownerHandle.getPointer());
                if (!WindowsWaypointWindow.CaptureApi.INSTANCE.SetWindowDisplayAffinity(
                        configHandle, WDA_EXCLUDEFROMCAPTURE)) {
                    throw new IllegalStateException(
                            "Cannot exclude config window from capture: " + Native.getLastError());
                }
                frame.setVisible(true);
//                User32.INSTANCE.SetWindowPos(
//                        configHandle,
//                        new WinDef.HWND(Pointer.createConstant(-1)),
//                        0,
//                        0,
//                        0,
//                        0,
//                        WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE);
                // The native owner keeps this above Minecraft without making it topmost over unrelated apps.
                frame.toFront();
            } catch (RuntimeException | LinkageError error) {
                closeOnEventThread();
                reportFailure(client, error);
            }
        });
    }

    private static JFrame createFrame(CheatConfig activeCheatConfig) {
        JFrame result = new JFrame("pvputils config");
        result.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        result.setResizable(false);
        result.setType(Window.Type.UTILITY);
        result.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                if (frame == event.getWindow()) {
                    frame = null;
                }
            }
        });
        result.getRootPane().registerKeyboardAction(
                _ -> result.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel grid = new JPanel(new GridLayout(0, 4, 8, 8));
        grid.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        addCheckBox(grid, "togglesneak gui", () -> config.isToggleSneakGuiEnabled,
                value -> config.isToggleSneakGuiEnabled = value,
                "modified version of the classic hcf togglesneak's gui");
        addCheckBox(grid, "movement toggle mirror press cancel", () -> config.isMovementToggleMirrorPressDisabling,
                value -> config.isMovementToggleMirrorPressDisabling = value,
                "disables movement toggle when autorun movement keys are re-pressed");
        addCheckBox(grid, "ignore backward after sprint reset", () -> config.isBackwardSprintResetSuppressionEnabled,
                value -> config.isBackwardSprintResetSuppressionEnabled = value,
                "allows one W+S sprint reset after each hit, then ignores Back while W is held until the next hit");
        addCheckBox(grid, "disable view bobbing camera shake", () -> config.isViewBobbingCameraShakeDisabled,
                value -> config.isViewBobbingCameraShakeDisabled = value,
                "keeps view bobbing enabled while removing only the camera shake");
        addCheckBox(grid, "mark targeted teammates on external overlay", () -> config.isTeammateTargetCrosshairMarkerEnabled,
                value -> config.isTeammateTargetCrosshairMarkerEnabled = value,
                "draws a red X over the crosshair on the external player waypoint overlay when targeting a teammate");
        addCheckBox(grid, "damage taken value notification", () -> config.isDamageTakenValueNotificationEnabled,
                value -> config.isDamageTakenValueNotificationEnabled = value, "");
        addCheckBox(grid, "targeting margin revert", () -> activeCheatConfig.isTargetingMarginReverted,
                value -> activeCheatConfig.isTargetingMarginReverted = value,
                "will flag hard on versions that use smaller hitboxes");
        addFloatField(grid, "targeting margin (static)", activeCheatConfig.staticTargetingMarginBypass,
                value -> activeCheatConfig.staticTargetingMarginBypass = value,
                "targeting margin bypass while standing still; invalid input is shown in red");
        addFloatField(grid, "targeting margin (moving)", activeCheatConfig.movingTargetMarginBypass,
                value -> activeCheatConfig.movingTargetMarginBypass = value,
                "targeting margin bypass while moving; invalid input is shown in red");
        addButton(grid, "reset ally nameplates", () -> {
            config.nameplateUuids.values().removeIf(each -> each == Config.NameplateTeam.ALLY);
            saveAll();
        }, "");
        addCheckBox(grid, "combat cheats", () -> config.isCheatsEnabled,
                value -> config.isCheatsEnabled = value, "");
        addWaypointCategoryButton(grid);
        addCheckBox(grid, "debug mode", () -> config.isDebugModeEnabled,
                value -> config.isDebugModeEnabled = value, "");
        addCheckBox(grid, "parkour cheat", () -> config.isParkourCheatEnabled,
                value -> config.isParkourCheatEnabled = value, "");
        addCheckBox(grid, "suppress teammate swings", () -> config.isTeammatesSwingSuppressionEnabled,
                value -> config.isTeammatesSwingSuppressionEnabled = value, "");
        result.setContentPane(grid);
        return result;
    }

    private static void addCheckBox(
            JPanel panel,
            String label,
            BooleanSupplier getter,
            Consumer<Boolean> setter,
            String tooltip) {
        JCheckBox box = new JCheckBox(label, getter.getAsBoolean());
        configure(box, tooltip);
        box.addActionListener(_ -> onClientThread(() -> {
            setter.accept(box.isSelected());
            saveAll();
        }));
        panel.add(box);
    }

    private static void addFloatField(
            JPanel panel,
            String label,
            float value,
            Consumer<Float> setter,
            String tooltip) {
        JTextField field = new JTextField(Float.toString(value));
        field.setName(label);
        configure(field, tooltip);
        Color validTextColor = field.getForeground();
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() {
                try {
                    float parsed = Float.parseFloat(field.getText());
                    if (!Float.isFinite(parsed)) {
                        throw new NumberFormatException("non-finite float");
                    }
                    field.setForeground(validTextColor);
                    onClientThread(() -> {
                        setter.accept(parsed);
                        saveAll();
                    });
                } catch (NumberFormatException ignored) {
                    field.setForeground(new Color(0xFF5555));
                }
            }

            @Override public void insertUpdate(DocumentEvent event) { changed(); }
            @Override public void removeUpdate(DocumentEvent event) { changed(); }
            @Override public void changedUpdate(DocumentEvent event) { changed(); }
        });
        panel.add(field);
    }

    private static void addButton(JPanel panel, String label, Runnable action, String tooltip) {
        JButton button = new JButton(label);
        configure(button, tooltip);
        button.addActionListener(_ -> onClientThread(action));
        panel.add(button);
    }

    private static void addWaypointCategoryButton(JPanel panel) {
        JButton button = new JButton(waypointCategoryLabel());
        configure(button, "cycles which player waypoints appear");
        button.addActionListener(_ -> onClientThread(() -> {
            Config.PlayerWaypointCategory[] values = Config.PlayerWaypointCategory.values();
            config.playerWaypointCategory = values[
                    (config.playerWaypointCategory.ordinal() + 1) % values.length];
            saveAll();
            SwingUtilities.invokeLater(() -> button.setText(waypointCategoryLabel()));
        }));
        panel.add(button);
    }

    private static String waypointCategoryLabel() {
        return "player waypoints: " + config.playerWaypointCategory.name();
    }

    private static void configure(JComponent component, String tooltip) {
        component.setPreferredSize(new Dimension(150, 28));
        component.setToolTipText(tooltip);
        component.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
    }

    private static void onClientThread(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    private static void saveAll() {
        config.saveConfig();
        serializeJsonBlocking("cheat-configs", cheatConfigs);
    }

    private static void reportFailure(Minecraft client, Throwable error) {
        LoggerFactory.getLogger("ExternalConfigWindow").error(
                "External config disabled: protected window unavailable", error);
        client.execute(() -> {
            if (client.player instanceof LocalPlayer player) {
                player.sendSystemMessage(Component.literal(
                        "External config unavailable; see the log for details"));
            }
        });
    }

    public static void close() {
        if (SwingUtilities.isEventDispatchThread()) {
            closeOnEventThread();
        } else {
            SwingUtilities.invokeLater(ExternalConfigWindow::closeOnEventThread);
        }
    }

    private static void closeOnEventThread() {
        if (frame != null) {
            JFrame closing = frame;
            frame = null;
            closing.dispose();
        }
    }
}
// codex end
