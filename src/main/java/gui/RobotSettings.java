package gui;

import java.awt.Color;

public class RobotSettings {
    private Color robotColor = Color.MAGENTA;
    private double maxVelocity = 0.1;
    private double maxAngularVelocity = 0.001;
    private RobotShape shape = RobotShape.OVAL;
    private float transparency = 1.0f;

    public Color getRobotColor() {
        return new Color(
                robotColor.getRed(),
                robotColor.getGreen(),
                robotColor.getBlue(),
                (int)(transparency * 255)
        );
    }

    public void setRobotColor(Color color) {
        this.robotColor = color;
    }

    public double getMaxVelocity() { return maxVelocity; }
    public void setMaxVelocity(double velocity) { this.maxVelocity = velocity; }

    public double getMaxAngularVelocity() { return maxAngularVelocity; }
    public void setMaxAngularVelocity(double velocity) { this.maxAngularVelocity = velocity; }

    public RobotShape getShape() { return shape; }
    public void setShape(RobotShape shape) { this.shape = shape; }

    public void setTransparency(float value) {
        this.transparency = Math.max(0, Math.min(1, value));
    }
}