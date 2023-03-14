package util;

public class Point {

    public double x, y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    public static Point getRandPos(Point center, double radius, double minDist) {
        double r = radius * Math.sqrt(Math.random()) + minDist; // distance
        double theta = Math.random() * 2 * Math.PI; // direction
        double x = center.x + r * Math.cos(theta); // cartesian pos x
        double y = center.y + r * Math.sin(theta); // cartesian pos y
        return new Point(x, y);
    }
}