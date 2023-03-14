package task3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Formatter;

public class Util {
    public static void printArrays(Body[] bodies, int gnumBodies, int numResultsShown) {
        int results = Math.min(numResultsShown, gnumBodies);
        if (numResultsShown <= 0) results = gnumBodies;

        Formatter fmt = new Formatter();

        fmt.format("p %3s %10s %10s %12s %12s\n", "i", "px", "py", "vx", "vy");
        fmt.format("-----------------------------------------------------\n");
        for (int i = 0; i < results; i++) {
            fmt.format("p %3s %10s %10s %12s %12s\n",
                    i,
                    round(bodies[i].x, 2),
                    round(bodies[i].y, 2),
                    round(bodies[i].vx, 7),
                    round(bodies[i].vy, 7));
        }
        fmt.format("-----------------------------------------------------\n");
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
