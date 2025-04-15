package gui;

import javax.swing.JFrame;

public record MainFrameState(WindowGeometry geometry, int extendedState) {
    public static MainFrameState fromFrame(JFrame frame) {
        return new MainFrameState(
                WindowGeometry.fromComponent(frame),
                frame.getExtendedState()
        );
    }
}
