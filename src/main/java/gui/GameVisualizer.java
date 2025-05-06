package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel
{
    private final Timer m_timer = initTimer();
    private final RobotSettings robotSettings = new RobotSettings();
    private int robotSize = 30;

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private volatile double m_robotPositionX = 100;
    private volatile double m_robotPositionY = 100;
    private volatile double m_robotDirection = 0;

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    public GameVisualizer()
    {
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onRedrawEvent();
            }
        }, 0, 50);
        m_timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                onModelUpdateEvent();
            }
        }, 0, 10);
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                setTargetPosition(e.getPoint());
                repaint();
            }
        });
        setDoubleBuffered(true);
    }

    public void setRobotSize(int size) {
        this.robotSize = size;
        repaint();
    }

    public RobotSettings getRobotSettings() {
        return robotSettings;
    }

    protected void setTargetPosition(Point p)
    {
        m_targetPositionX = p.x;
        m_targetPositionY = p.y;
    }

    protected void onRedrawEvent()
    {
        EventQueue.invokeLater(this::repaint);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        double diffX = toX - fromX;
        double diffY = toY - fromY;

        return asNormalizedRadians(Math.atan2(diffY, diffX));
    }

    protected void onModelUpdateEvent()
    {
        checkBoundaries();
        double distance = distance(m_targetPositionX, m_targetPositionY,
                m_robotPositionX, m_robotPositionY);
        if (distance < 0.5)
        {
            return;
        }
        double velocity = robotSettings.getMaxVelocity();
        double angleToTarget = angleTo(m_robotPositionX, m_robotPositionY, m_targetPositionX, m_targetPositionY);
        double angularVelocity = 0;
        if (angleToTarget > m_robotDirection)
        {
            angularVelocity = robotSettings.getMaxAngularVelocity();
        }
        if (angleToTarget < m_robotDirection)
        {
            angularVelocity = -robotSettings.getMaxAngularVelocity();
        }

        moveRobot(velocity, angularVelocity, 10);
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private void moveRobot(double velocity, double angularVelocity, double duration)
    {
        velocity = applyLimits(velocity, 0, robotSettings.getMaxVelocity());
        angularVelocity = applyLimits(angularVelocity, -robotSettings.getMaxAngularVelocity(), robotSettings.getMaxAngularVelocity());
        double newX = m_robotPositionX + velocity / angularVelocity *
                (Math.sin(m_robotDirection  + angularVelocity * duration) -
                        Math.sin(m_robotDirection));
        if (!Double.isFinite(newX))
        {
            newX = m_robotPositionX + velocity * duration * Math.cos(m_robotDirection);
        }
        double newY = m_robotPositionY - velocity / angularVelocity *
                (Math.cos(m_robotDirection  + angularVelocity * duration) -
                        Math.cos(m_robotDirection));
        if (!Double.isFinite(newY))
        {
            newY = m_robotPositionY + velocity * duration * Math.sin(m_robotDirection);
        }
        m_robotPositionX = newX;
        m_robotPositionY = newY;
        double newDirection = asNormalizedRadians(m_robotDirection + angularVelocity * duration);
        m_robotDirection = newDirection;
    }

    private static double asNormalizedRadians(double angle)
    {
        while (angle < 0)
        {
            angle += 2*Math.PI;
        }
        while (angle >= 2*Math.PI)
        {
            angle -= 2*Math.PI;
        }
        return angle;
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d, round(m_robotPositionX), round(m_robotPositionY), m_robotDirection);
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g, int x, int y, double direction) {
        int robotCenterX = round(m_robotPositionX);
        int robotCenterY = round(m_robotPositionY);
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(robotSettings.getRobotColor());

        int width = robotSize;
        int height = robotSize / 3;

        switch (robotSettings.getShape()) {
            case OVAL:
                fillOval(g, robotCenterX, robotCenterY, width, height);
                break;
            case RECTANGLE:
                g.fillRect(robotCenterX - width/2, robotCenterY - height/2, width, height);
                break;
            case TRIANGLE:
                int[] xPoints = {robotCenterX, robotCenterX + width/2, robotCenterX - width/2};
                int[] yPoints = {robotCenterY - height, robotCenterY + height/2, robotCenterY + height/2};
                g.fillPolygon(xPoints, yPoints, 3);
                break;
        }

        g.setColor(Color.BLACK);
        switch (robotSettings.getShape()) {
            case OVAL:
                drawOval(g, robotCenterX, robotCenterY, width, height);
                break;
            case RECTANGLE:
                g.drawRect(robotCenterX - width/2, robotCenterY - height/2, width, height);
                break;
            case TRIANGLE:
                int[] xPoints = {robotCenterX, robotCenterX + width/2, robotCenterX - width/2};
                int[] yPoints = {robotCenterY - height, robotCenterY + height/2, robotCenterY + height/2};
                g.drawPolygon(xPoints, yPoints, 3);
                break;
        }

        g.setColor(Color.WHITE);
        fillOval(g, robotCenterX + width/3, robotCenterY, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, robotCenterX + width/3, robotCenterY, 5, 5);
    }

    private void drawTarget(Graphics2D g, int x, int y)
    {
        AffineTransform t = AffineTransform.getRotateInstance(0, 0, 0);
        g.setTransform(t);
        g.setColor(Color.GREEN);
        fillOval(g, x, y, 5, 5);
        g.setColor(Color.BLACK);
        drawOval(g, x, y, 5, 5);
    }

    private void checkBoundaries() {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        double robotSize = this.robotSize;

        if (m_robotPositionX < robotSize /2) {
            m_robotDirection = Math.PI - m_robotDirection;
            m_robotPositionX = robotSize /2;
        }
        else if (m_robotPositionX > panelWidth - robotSize /2) {
            m_robotDirection = Math.PI - m_robotDirection;
            m_robotPositionX = panelWidth - robotSize/2;
        }

        if (m_robotPositionY < robotSize/2) {
            m_robotDirection = -m_robotDirection;
            m_robotPositionY = robotSize/2;
        }
        else if (m_robotPositionY > panelHeight - robotSize/2) {
            m_robotDirection = -m_robotDirection;
            m_robotPositionY = panelHeight - robotSize/2;
        }

        m_robotDirection = asNormalizedRadians(m_robotDirection);
    }
}
