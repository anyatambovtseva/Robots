package gui;

import java.awt.*;

public record WindowGeometry(int x, int y, int width, int height) {
    public static WindowGeometry fromComponent(Component frame) {
        return new WindowGeometry(frame.getX(), frame.getY(),
                frame.getWidth(), frame.getHeight());
    }
}