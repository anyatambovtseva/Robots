package gui;

import java.awt.Component;

public record WindowGeometry(int x, int y, int width, int height) {
    public static WindowGeometry fromComponent(Component component) {
        return new WindowGeometry(
                component.getX(),
                component.getY(),
                component.getWidth(),
                component.getHeight()
        );
    }
}