package gui;

import java.awt.Color;

public class RobotModel {
    private double positionX = 100;
    private double positionY = 100;
    private double direction = 0;
    private final RobotSettings settings = new RobotSettings();
    private int size = 30;

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public void setDirection(double direction) {
        this.direction = direction;
    }

    public void move(double velocity, double angularVelocity, double duration) {
        velocity = applyLimits(velocity, 0, settings.getMaxVelocity());
        angularVelocity = applyLimits(angularVelocity, -settings.getMaxAngularVelocity(), settings.getMaxAngularVelocity());

        double newX = positionX + velocity / angularVelocity *
                (Math.sin(direction + angularVelocity * duration) -
                        Math.sin(direction));
        if (!Double.isFinite(newX)) {
            newX = positionX + velocity * duration * Math.cos(direction);
        }

        double newY = positionY - velocity / angularVelocity *
                (Math.cos(direction + angularVelocity * duration) -
                        Math.cos(direction));
        if (!Double.isFinite(newY)) {
            newY = positionY + velocity * duration * Math.sin(direction);
        }

        positionX = newX;
        positionY = newY;
        direction = asNormalizedRadians(direction + angularVelocity * duration);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public RobotSettings getSettings() {
        return settings;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getDirection() {
        return direction;
    }

    private static double applyLimits(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static double asNormalizedRadians(double angle) {
        while (angle < 0) angle += 2*Math.PI;
        while (angle >= 2*Math.PI) angle -= 2*Math.PI;
        return angle;
    }
}