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
    private final RobotModel robotModel;

    private static Timer initTimer()
    {
        Timer timer = new Timer("events generator", true);
        return timer;
    }

    private volatile int m_targetPositionX = 150;
    private volatile int m_targetPositionY = 100;

    public GameVisualizer()
    {
        this.robotModel = new RobotModel();
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
        robotModel.setSize(size);
        repaint();
    }

    public RobotSettings getRobotSettings() {
        return robotModel.getSettings();
    }

    public RobotModel getRobotModel() {
        return robotModel;
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
                robotModel.getPositionX(), robotModel.getPositionY());
        if (distance < 0.5)
        {
            return;
        }
        double velocity = robotModel.getSettings().getMaxVelocity();
        double angleToTarget = angleTo(robotModel.getPositionX(), robotModel.getPositionY(), m_targetPositionX, m_targetPositionY);
        double angularVelocity = 0;
        if (angleToTarget > robotModel.getDirection())
        {
            angularVelocity = robotModel.getSettings().getMaxAngularVelocity();
        }
        if (angleToTarget < robotModel.getDirection())
        {
            angularVelocity = -robotModel.getSettings().getMaxAngularVelocity();
        }

        robotModel.move(velocity, angularVelocity, 10);
    }

    private static int round(double value)
    {
        return (int)(value + 0.5);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private void drawRobot(Graphics2D g) {
        int robotCenterX = round(robotModel.getPositionX());
        int robotCenterY = round(robotModel.getPositionY());
        double direction = robotModel.getDirection();
        AffineTransform t = AffineTransform.getRotateInstance(direction, robotCenterX, robotCenterY);
        g.setTransform(t);
        g.setColor(robotModel.getSettings().getRobotColor());

        int width = robotModel.getSize();
        int height = robotModel.getSize() / 3;

        switch (robotModel.getSettings().getShape()) {
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
        switch (robotModel.getSettings().getShape()) {
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
        double robotSize = robotModel.getSize();

        if (robotModel.getPositionX() < robotSize /2) {
            robotModel.setDirection(Math.PI - robotModel.getDirection());
            robotModel.setPositionX(robotSize /2);
        }
        else if (robotModel.getPositionX() > panelWidth - robotSize /2) {
            robotModel.setDirection(Math.PI - robotModel.getDirection());
            robotModel.setPositionX(panelWidth - robotSize/2);
        }

        if (robotModel.getPositionY() < robotSize/2) {
            robotModel.setDirection(-robotModel.getDirection());
            robotModel.setPositionY(robotSize/2);
        }
        else if (robotModel.getPositionY() > panelHeight - robotSize/2) {
            robotModel.setDirection(-robotModel.getDirection());
            robotModel.setPositionY(panelHeight - robotSize/2);
        }

        robotModel.setDirection(asNormalizedRadians(robotModel.getDirection()));
    }

    private static double asNormalizedRadians(double angle) {
        while (angle < 0) angle += 2*Math.PI;
        while (angle >= 2*Math.PI) angle -= 2*Math.PI;
        return angle;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D)g;
        drawRobot(g2d);
        drawTarget(g2d, m_targetPositionX, m_targetPositionY);
    }
}
