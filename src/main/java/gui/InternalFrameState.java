package gui;

import javax.swing.JInternalFrame;

public record InternalFrameState(WindowGeometry geometry,
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