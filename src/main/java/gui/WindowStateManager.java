package gui;

import log.Logger;

import javax.swing.*;
import java.awt.*;
import java.lang.annotation.*;
import java.util.prefs.Preferences;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface PersistWindowState {
    boolean value() default true;
}

public class WindowStateManager {
    private static final String PREF_NODE = "/robots_game/window_states";
    private final Preferences prefs;
    private final JFrame mainFrame;
    private final JDesktopPane desktopPane;

    private record WindowGeometry(int x, int y, int width, int height) {
        public static WindowGeometry fromComponent(Component frame) {
            return new WindowGeometry(frame.getX(), frame.getY(),
                    frame.getWidth(), frame.getHeight());
        }
    }

    private record MainFrameState(WindowGeometry geometry, int extendedState) {
        public static MainFrameState fromFrame(JFrame frame) {
            return new MainFrameState(
                    WindowGeometry.fromComponent(frame),
                    frame.getExtendedState()
            );
        }
    }

    private boolean shouldPersist(Component component) {
        PersistWindowState annotation = component.getClass().getAnnotation(PersistWindowState.class);
        return annotation == null || annotation.value();
    }

    private record InternalFrameState(WindowGeometry geometry,
                                      boolean isMinimized,
                                      boolean isMaximized) {
        public static InternalFrameState fromFrame(JInternalFrame frame) {
            return new InternalFrameState(
                    WindowGeometry.fromComponent(frame),
                    frame.isIcon(),
                    frame.isMaximum()
            );
        }
    }

    public WindowStateManager(JFrame mainFrame, JDesktopPane desktopPane) {
        this.mainFrame = mainFrame;
        this.desktopPane = desktopPane;
        this.prefs = Preferences.userRoot().node(PREF_NODE);
    }

    public void loadWindowStates() {
        try {
            if (shouldPersist(mainFrame)) {
                loadFrameStateWithReflection(mainFrame, "main");
            }

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame && shouldPersist(component)) {
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
            loadFrameStateWithReflection(frame, prefix);
        } catch (Exception e) {
            Logger.error("Error loading window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    private void loadFrameStateWithReflection(Component frame, String prefix) throws Exception {
        try {
            WindowGeometry geometry = new WindowGeometry(
                    prefs.getInt(prefix + ".x", frame.getX()),
                    prefs.getInt(prefix + ".y", frame.getY()),
                    prefs.getInt(prefix + ".width", frame.getWidth()),
                    prefs.getInt(prefix + ".height", frame.getHeight())
            );

            frame.setBounds(geometry.x(), geometry.y(),
                    geometry.width(), geometry.height());

            if (frame instanceof JFrame) {
                int state = prefs.getInt(prefix + ".state", Frame.NORMAL);
                ((JFrame) frame).setExtendedState(state);
            } else if (frame instanceof JInternalFrame) {
                boolean isMinimizedToIcon = prefs.getBoolean(prefix + ".isMinimized", false);
                boolean isMaximized = prefs.getBoolean(prefix + ".isMaximized", false);
                ((JInternalFrame) frame).setIcon(isMinimizedToIcon);
                ((JInternalFrame) frame).setMaximum(isMaximized);
            }
        } catch (Exception e) {
            throw new Exception("Failed to load frame state with reflection: " + e.getMessage(), e);
        }
    }

    public void saveWindowStates() {
        try {
            if (shouldPersist(mainFrame)) {
                saveFrameStateWithReflection(mainFrame, "main");
            }

            for (Component component : desktopPane.getComponents()) {
                if (component instanceof JInternalFrame && shouldPersist(component)) {
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
            saveFrameStateWithReflection(frame, prefix);
        } catch (Exception e) {
            Logger.error("Error saving window state for " + frame.getTitle() + ": " + e.getMessage());
        }
    }

    private void saveFrameStateWithReflection(Component frame, String prefix) throws Exception {
        try {
            if (frame instanceof JFrame) {
                MainFrameState state = MainFrameState.fromFrame((JFrame) frame);
                saveGeometry(state.geometry(), prefix);
                prefs.putInt(prefix + ".state", state.extendedState());
            } else if (frame instanceof JInternalFrame) {
                InternalFrameState state = InternalFrameState.fromFrame((JInternalFrame) frame);
                saveGeometry(state.geometry(), prefix);
                prefs.putBoolean(prefix + ".isMinimized", state.isMinimized());
                prefs.putBoolean(prefix + ".isMaximized", state.isMaximized());
            }
        } catch (Exception e) {
            throw new Exception("Failed to save frame state: " + e.getMessage(), e);
        }
    }

    private void saveGeometry(WindowGeometry geometry, String prefix) {
        prefs.putInt(prefix + ".x", geometry.x());
        prefs.putInt(prefix + ".y", geometry.y());
        prefs.putInt(prefix + ".width", geometry.width());
        prefs.putInt(prefix + ".height", geometry.height());
    }

    private Object invokeGetter(Object obj, String methodName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = obj.getClass().getMethod(methodName);
        return method.invoke(obj);
    }

    private void invokeSetter(Object obj, String methodName, Class<?>[] paramTypes, Object[] args)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = obj.getClass().getMethod(methodName, paramTypes);
        method.invoke(obj, args);
    }
}