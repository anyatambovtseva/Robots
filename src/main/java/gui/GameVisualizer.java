package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

public class GameVisualizer extends JPanel {
    private final int CELL_SIZE = 10;
    private final Timer m_timer = initTimer();
    private final RobotSettings robotSettings = new RobotSettings();
    private int robotSize = 30;
    private final RobotModel robotModel = new RobotModel();

    private boolean[][] maze;
    private List<Point> currentPath = new ArrayList<>();
    private javax.swing.Timer pathAnimationTimer;

    private Point robotCell = new Point(5, 5);
    private Point targetCell = new Point(10, 5);

    public GameVisualizer() {
        initializeMaze(50, 50);

        pathAnimationTimer = new javax.swing.Timer(100, e -> {
            if (!currentPath.isEmpty()) {
                robotCell = currentPath.remove(0);
                robotModel.setPositionX(robotCell.x * CELL_SIZE + CELL_SIZE/2);
                robotModel.setPositionY(robotCell.y * CELL_SIZE + CELL_SIZE/2);
                repaint();
            } else {
                pathAnimationTimer.stop();
            }
        });

        m_timer.schedule(new TimerTask() {
            @Override
            public void run() { onRedrawEvent(); }
        }, 0, 50);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point clickCell = new Point(e.getX()/CELL_SIZE, e.getY()/CELL_SIZE);

                if (!isWall(clickCell.x, clickCell.y)) {
                    targetCell = clickCell;
                    calculatePathToTarget();
                    repaint();
                }
            }
        });

        setDoubleBuffered(true);
    }

    private void initializeMaze(int width, int height) {
        MazeGenerator generator = new MazeGenerator(width, height, robotModel);
        this.maze = generator.getMaze();

        robotModel.setPositionX(robotCell.x * CELL_SIZE + CELL_SIZE/2);
        robotModel.setPositionY(robotCell.y * CELL_SIZE + CELL_SIZE/2);
    }

    private boolean isWall(int x, int y) {
        return x < 0 || y < 0 || x >= maze.length || y >= maze[0].length || maze[x][y];
    }

    private void calculatePathToTarget() {
        AStarPathFinder finder = new AStarPathFinder(maze, robotModel);
        currentPath = finder.findPath(targetCell);

        if (!currentPath.isEmpty()) {
            pathAnimationTimer.start();
        }
    }

    private static Timer initTimer() {
        return new Timer("events generator", true);
    }

    protected void onRedrawEvent() {
        EventQueue.invokeLater(this::repaint);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        drawMaze(g2d);
        drawPath(g2d);
        drawTarget(g2d);
        drawRobot(g2d);
    }

    private void drawMaze(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        for (int x = 0; x < maze.length; x++) {
            for (int y = 0; y < maze[0].length; y++) {
                if (maze[x][y]) {
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawPath(Graphics2D g2d) {
        if (!currentPath.isEmpty()) {
            g2d.setColor(new Color(100, 100, 255, 150));
            for (Point cell : currentPath) {
                g2d.fillRect(cell.x * CELL_SIZE, cell.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawTarget(Graphics2D g2d) {
        int targetX = targetCell.x * CELL_SIZE + CELL_SIZE/2;
        int targetY = targetCell.y * CELL_SIZE + CELL_SIZE/2;

        g2d.setColor(Color.GREEN);
        fillOval(g2d, targetX, targetY, 8, 8);
        g2d.setColor(Color.BLACK);
        drawOval(g2d, targetX, targetY, 8, 8);
    }

    private void drawRobot(Graphics2D g2d) {
        int robotX = robotCell.x * CELL_SIZE + CELL_SIZE/2;
        int robotY = robotCell.y * CELL_SIZE + CELL_SIZE/2;

        AffineTransform oldTransform = g2d.getTransform();

        if (!currentPath.isEmpty()) {
            Point nextCell = currentPath.get(0);
            double angle = Math.atan2(nextCell.y - robotCell.y, nextCell.x - robotCell.x);
            g2d.rotate(angle, robotX, robotY);
        }

        g2d.setColor(robotSettings.getRobotColor());

        switch (robotSettings.getShape()) {
            case OVAL:
                fillOval(g2d, robotX, robotY, robotSize, robotSize/2);
                g2d.setColor(Color.BLACK);
                drawOval(g2d, robotX, robotY, robotSize, robotSize/2);
                break;

            case RECTANGLE:
                g2d.fillRect(robotX - robotSize/2, robotY - robotSize/4, robotSize, robotSize/2);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(robotX - robotSize/2, robotY - robotSize/4, robotSize, robotSize/2);
                break;

            case TRIANGLE:
                int[] xPoints = {robotX, robotX + robotSize/2, robotX - robotSize/2};
                int[] yPoints = {robotY - robotSize/3, robotY + robotSize/4, robotY + robotSize/4};
                g2d.fillPolygon(xPoints, yPoints, 3);
                g2d.setColor(Color.BLACK);
                g2d.drawPolygon(xPoints, yPoints, 3);
                break;
        }

        g2d.setColor(Color.WHITE);
        fillOval(g2d, robotX + robotSize/4, robotY - robotSize/8, robotSize/4, robotSize/4);

        g2d.setTransform(oldTransform);
    }

    private static void fillOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.fillOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    private static void drawOval(Graphics g, int centerX, int centerY, int diam1, int diam2)
    {
        g.drawOval(centerX - diam1 / 2, centerY - diam2 / 2, diam1, diam2);
    }

    public void setRobotSize(int size) {
        this.robotSize = size;
        repaint();
    }

    public RobotSettings getRobotSettings() {
        return robotSettings;
    }
}
