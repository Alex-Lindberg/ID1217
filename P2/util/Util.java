package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Formatter;

public class Util {

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
    
    public static void printArrays(Body[] bodies, int gnumBodies, int numResultsShown) {
        int results = Math.min(numResultsShown, gnumBodies);
        if (numResultsShown <= 0) results = gnumBodies;

        Formatter fmt = new Formatter();

        fmt.format("p %3s %12s %12s %12s %12s\n", "i", "px", "py", "vx", "vy");
        fmt.format("-------------------------------------------------\n");
        for (int i = 0; i < results; i++) {
            fmt.format("p %3s %12s %12s %12s %12s\n",
                    i,
                    round(bodies[i].x, 4),
                    round(bodies[i].y, 4),
                    round(bodies[i].vx, 4),
                    round(bodies[i].vy, 4));
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
