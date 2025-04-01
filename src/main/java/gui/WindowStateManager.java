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
            int mainX = prefs.getInt("main.x", mainFrame.getX());
            int mainY = prefs.getInt("main.y", mainFrame.getY());
            int mainWidth = prefs.getInt("main.width", mainFrame.getWidth());
            int mainHeight = prefs.getInt("main.height", mainFrame.getHeight());
            int mainState = prefs.getInt("main.state", Frame.NORMAL);

            mainFrame.setBounds(mainX, mainY, mainWidth, mainHeight);
            mainFrame.setExtendedState(mainState);

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame) {
                    loadInternalFrameState((JInternalFrame) component);
                }
            }
        } catch (Exception e) {
            Logger.error("Error loading window states: " + e.getMessage());
        }
    }

    private void loadInternalFrameState(JInternalFrame frame) {
        try {
            String prefix = "window." + frame.getTitle().replace(" ", "_");

            int x = prefs.getInt(prefix + ".x", frame.getX());
            int y = prefs.getInt(prefix + ".y", frame.getY());
            int width = prefs.getInt(prefix + ".width", frame.getWidth());
            int height = prefs.getInt(prefix + ".height", frame.getHeight());
            boolean isIcon = prefs.getBoolean(prefix + ".icon", false);
            boolean isMaximized = prefs.getBoolean(prefix + ".maximized", false);

            frame.setBounds(x, y, width, height);
            frame.setIcon(isIcon);
            frame.setMaximum(isMaximized);
        } catch (Exception e) {
            Logger.error("Error loading window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    public void saveWindowStates() {
        try {
            prefs.putInt("main.x", mainFrame.getX());
            prefs.putInt("main.y", mainFrame.getY());
            prefs.putInt("main.width", mainFrame.getWidth());
            prefs.putInt("main.height", mainFrame.getHeight());
            prefs.putInt("main.state", mainFrame.getExtendedState());

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame) {
                    saveInternalFrameState((JInternalFrame) component);
                }
            }

            prefs.flush();
        } catch (Exception e) {
            Logger.error("Error saving window states: " + e.getMessage());
        }
    }

    private void saveInternalFrameState(JInternalFrame frame) {
        try {
            String prefix = "window." + frame.getTitle().replace(" ", "_");

            prefs.putInt(prefix + ".x", frame.getX());
            prefs.putInt(prefix + ".y", frame.getY());
            prefs.putInt(prefix + ".width", frame.getWidth());
            prefs.putInt(prefix + ".height", frame.getHeight());
            prefs.putBoolean(prefix + ".icon", frame.isIcon());
            prefs.putBoolean(prefix + ".maximized", frame.isMaximum());
        } catch (Exception e) {
            Logger.error("Error saving window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }
}