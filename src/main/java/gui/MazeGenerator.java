package gui;

import java.util.*;

public class MazeGenerator {
    private final int width;
    private final int height;
    private final boolean[][] maze;
    private final Random random = new Random();
    private final RobotModel robotModel;

    public MazeGenerator(int width, int height, RobotModel robotModel) {
        this.width = width;
        this.height = height;
        this.maze = new boolean[width][height];
        this.robotModel = robotModel;
        generate();
    }

    private void generate() {
        for (int x = 0; x < width; x++) {
            Arrays.fill(maze[x], true);
        }

        List<int[]> walls = new ArrayList<>();

        int startX = (int)(robotModel.getPositionX() / 10);
        int startY = (int)(robotModel.getPositionY() / 10);

        startX = Math.max(1, Math.min(width - 2, startX));
        startY = Math.max(1, Math.min(height - 2, startY));

        maze[startX][startY] = false;
        addWalls(startX, startY, walls);

        while (!walls.isEmpty()) {
            int[] wall = walls.remove(random.nextInt(walls.size()));
            int x = wall[0], y = wall[1];

            if (countEmptyNeighbors(x, y) == 1) {
                maze[x][y] = false;
                addWalls(x, y, walls);
            }
        }
    }

    private void addWalls(int x, int y, List<int[]> walls) {
        for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int nx = x + dir[0], ny = y + dir[1];
            if (nx > 0 && nx < width-1 && ny > 0 && ny < height-1 && maze[nx][ny]) {
                walls.add(new int[]{nx, ny});
            }
        }
    }

    private int countEmptyNeighbors(int x, int y) {
        int count = 0;
        for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            if (!maze[x + dir[0]][y + dir[1]]) count++;
        }
        return count;
    }

    public boolean[][] getMaze() {
        return maze;
    }
}