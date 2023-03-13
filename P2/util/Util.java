package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Formatter;
import java.util.stream.Collectors;

public class Util {

    public static class Point {

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

    public static void printArrays(Point[] pPositions, Point[] pVelocities, int gnumBodies, int numResultsShown) {
        int results = Math.min(numResultsShown, gnumBodies);

        Formatter fmt = new Formatter();

        fmt.format("p %3s %10s %10s %10s %10s\n", "i", "px", "py", "vx", "vy");
        fmt.format("-------------------------------------------------\n");
        for (int i = 0; i < results; i++) {
            fmt.format("p %3s %10s %10s %10s %10s\n",
                    i,
                    round(pPositions[i].x, 2),
                    round(pPositions[i].y, 2),
                    round(pVelocities[i].x, 2),
                    round(pVelocities[i].y, 2));
        }
        fmt.format("-------------------------------------------------\n");
        fmt.format("Total Body count : %d\n", gnumBodies);
        System.out.println(fmt);
        
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
    
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    
}
