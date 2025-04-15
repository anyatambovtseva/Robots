package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class WindowStateManager {
    private static final String PREF_NODE = "/robots_game/window_states";
    private final Preferences prefs;
    private final JFrame mainFrame;
    private final JDesktopPane desktopPane;

    public WindowStateManager(JFrame mainFrame, JDesktopPane desktopPane) {
        this.mainFrame = mainFrame;
        this.desktopPane = desktopPane;
        this.prefs = Preferences.userRoot().node(PREF_NODE);
    }

    public void loadWindowStates() {
        try {
            if (shouldPersist(mainFrame)) {
                loadFrameState(mainFrame, WindowPrefKeys.getMainWindowPrefix());
            }

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame internalFrame && shouldPersist(internalFrame)) {
                    loadInternalFrameState(internalFrame);
                }
            }
        } catch (Exception e) {
            Logger.error("Error loading window states: " + e.getMessage());
        }
    }

    public void saveWindowStates() {
        try {
            if (shouldPersist(mainFrame)) {
                saveFrameState(mainFrame, WindowPrefKeys.getMainWindowPrefix());
            }

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame internalFrame && shouldPersist(internalFrame)) {
                    saveInternalFrameState(internalFrame);
                }
            }
            prefs.flush();
        } catch (Exception e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
    }

    private boolean shouldPersist(Component component) {
        PersistWindowState annotation = component.getClass().getAnnotation(PersistWindowState.class);
        return annotation != null && annotation.value();
    }

    private void loadInternalFrameState(JInternalFrame frame) {
        try {
            String prefix = WindowPrefKeys.getInternalWindowPrefix() + "." + frame.getTitle().replace(" ", "_");
            loadFrameState(frame, prefix);
        } catch (Exception e) {
            Logger.error("Error loading window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    private void loadFrameState(Component frame, String prefix) throws Exception {
        try {
            WindowGeometry geometry = new WindowGeometry(
                    prefs.getInt(WindowPrefKeys.X.getFullKey(prefix), frame.getX()),
                    prefs.getInt(WindowPrefKeys.Y.getFullKey(prefix), frame.getY()),
                    prefs.getInt(WindowPrefKeys.WIDTH.getFullKey(prefix), frame.getWidth()),
                    prefs.getInt(WindowPrefKeys.HEIGHT.getFullKey(prefix), frame.getHeight())
            );

            frame.setBounds(geometry.x(), geometry.y(), geometry.width(), geometry.height());

            if (frame instanceof JFrame jFrame) {
                int state = prefs.getInt(WindowPrefKeys.STATE.getFullKey(prefix), Frame.NORMAL);
                jFrame.setExtendedState(state);
            } else if (frame instanceof JInternalFrame internalFrame) {
                boolean isMinimized = prefs.getBoolean(WindowPrefKeys.IS_MINIMIZED.getFullKey(prefix), false);
                boolean isMaximized = prefs.getBoolean(WindowPrefKeys.IS_MAXIMIZED.getFullKey(prefix), false);
                internalFrame.setIcon(isMinimized);
                internalFrame.setMaximum(isMaximized);
            }
        } catch (Exception e) {
            throw new Exception("Failed to load frame state: " + e.getMessage(), e);
        }
    }

    private void saveInternalFrameState(JInternalFrame frame) {
        try {
            String prefix = WindowPrefKeys.getInternalWindowPrefix() + "." + frame.getTitle().replace(" ", "_");
            saveFrameState(frame, prefix);
        } catch (Exception e) {
            Logger.error("Error saving window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    private void saveFrameState(Component frame, String prefix) throws Exception {
        try {
            if (frame instanceof JFrame jFrame) {
                MainFrameState state = MainFrameState.fromFrame(jFrame);
                saveGeometry(state.geometry(), prefix);
                prefs.putInt(WindowPrefKeys.STATE.getFullKey(prefix), state.extendedState());
            } else if (frame instanceof JInternalFrame internalFrame) {
                InternalFrameState state = InternalFrameState.fromFrame(internalFrame);
                saveGeometry(state.geometry(), prefix);
                prefs.putBoolean(WindowPrefKeys.IS_MINIMIZED.getFullKey(prefix), state.isMinimized());
                prefs.putBoolean(WindowPrefKeys.IS_MAXIMIZED.getFullKey(prefix), state.isMaximized());
            }
        } catch (Exception e) {
            throw new Exception("Failed to save frame state: " + e.getMessage(), e);
        }
    }

    private void saveGeometry(WindowGeometry geometry, String prefix) {
        prefs.putInt(WindowPrefKeys.X.getFullKey(prefix), geometry.x());
        prefs.putInt(WindowPrefKeys.Y.getFullKey(prefix), geometry.y());
        prefs.putInt(WindowPrefKeys.WIDTH.getFullKey(prefix), geometry.width());
        prefs.putInt(WindowPrefKeys.HEIGHT.getFullKey(prefix), geometry.height());
    }
}