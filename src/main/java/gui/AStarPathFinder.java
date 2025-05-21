package gui;

import java.awt.Point;
import java.util.*;

public class AStarPathFinder {
    private final boolean[][] obstacles;
    private final RobotModel robotModel;

    public AStarPathFinder(boolean[][] obstacles, RobotModel robotModel) {
        this.obstacles = obstacles;
        this.robotModel = robotModel;
    }

    public List<Point> findPath(Point target) {
        Point start = new Point(
                (int)(robotModel.getPositionX() / 10),
                (int)(robotModel.getPositionY() / 10)
        );

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<Point, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, null, 0, heuristic(start, target));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            if (current.position.equals(target)) {
                return reconstructPath(current);
            }

            for (Point neighbor : getNeighbors(current.position)) {
                if (obstacles[neighbor.x][neighbor.y]) continue;

                double tentativeG = current.g + 1;
                Node neighborNode = allNodes.getOrDefault(neighbor, new Node(neighbor));

                if (tentativeG < neighborNode.g) {
                    neighborNode.parent = current;
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + heuristic(neighbor, target);
                    allNodes.put(neighbor, neighborNode);
                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private List<Point> reconstructPath(Node endNode) {
        LinkedList<Point> path = new LinkedList<>();
        Node current = endNode;
        while (current != null) {
            path.addFirst(current.position);
            current = current.parent;
        }
        return path;
    }

    private List<Point> getNeighbors(Point p) {
        List<Point> neighbors = new ArrayList<>();
        for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
            int x = p.x + dir[0], y = p.y + dir[1];
            if (x >= 0 && x < obstacles.length && y >= 0 && y < obstacles[0].length) {
                neighbors.add(new Point(x, y));
            }
        }
        return neighbors;
    }

    private double heuristic(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private static class Node implements Comparable<Node> {
        Point position;
        Node parent;
        double g;
        double f;

        public Node(Point position) {
            this(position, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        public Node(Point position, Node parent, double g, double f) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.f = f;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }
}