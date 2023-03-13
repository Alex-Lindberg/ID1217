/**
 * Nbody-Problem simulator
 * Sequential brute force version
 * 
 * Time complexity: O(N^2)
 * 
 * Usage (from root):
 *  javac task1/Nbody.java
 *  java task1.Nbody.java [gnumBodies] [numSteps] [massOfBodies] [massVariance] [numResultsShown]
 * 
 * where:
 *  gnumBodies: The number of bodies used in the simulation.
 *  numSteps: The number of steps/cycles.
 *  massOfBodies: The mass used for each body.
 *  numResultsShown: The number of results to be printed to stdout.
 * 
 * 400_000 steps roughly equates to 15 seconds execution time for 120 bodies
 * on my personal computer. (14887,8 ms median)
 * 
 * Some liberty was taken to make the results look nice when displayed as a figure.
 * 
 *  @author Alex Lindberg
 * 
 */
package task1;

import util.Util;
import util.Util.*;

public class Nbody {

    public static final double G = 6.67e-3;
    public static final Double EARTH_MASS = 59.742;
    public static final double RADIUS = 150;
    public static final double MIN_DIST = 80;
    public static final double SOFTENING = 1e5;
    public static final double DT = 0.1;
    public static final double START_VEL = 0.0008;

    public final int gnumBodies;
    public final int numSteps;
    public final double massBody;

    // Data oriented variable storage
    Point[] p; // x, y coords
    Point[] v; // x, y velocity
    Point[] f; // x, y Forces
    double[] ms; // Mass

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies   The number of bodies
     * @param numSteps     The number of calculation steps/cycles
     * @param massOfBodies The mass of every body
     * @param massVariance The variance of mass between bodies
     * @param vel          Velocity bounds of a body (see init. loop for more info).
     */
    public Nbody(int gnumBodies, int numSteps, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.massBody = massBody;

        this.p = new Point[gnumBodies];
        this.v = new Point[gnumBodies];
        this.f = new Point[gnumBodies];
        this.ms = new double[gnumBodies];

        this.p[0] = new Point(0, 0);
        this.v[0] = new Point(0, 0);
        this.f[0] = new Point(0, 0);
        this.ms[0] = EARTH_MASS * 333.0;

        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            this.p[i] = Point.getRandPos(this.p[0], RADIUS, MIN_DIST);
            // Random velocity between [-velBound, velBound]
            // Velocity orthogonal to the direction vector from the sun to current body
            double vx = (this.p[0].x - this.p[i].x) * START_VEL;
            double vy = -(this.p[0].y - this.p[i].x) * START_VEL;
            this.v[i] = new Point(vx, vy);
            this.f[i] = new Point(0, 0);
            // Mass with some varaince
            this.ms[i] = massBody * (1 + randInterval(-massVariance, massVariance));
        }
    }

    public void calculateForces() {
        double distance, mag;
        Point direction;

        for (int i = 0; i < gnumBodies; i++) {
            for (int j = i + 1; j < gnumBodies; j++) {
                distance = dist(p[i], p[j]);
                mag = (G * ms[i] * ms[j]) / (Math.pow(distance, 2) + SOFTENING);
                direction = new Point(  p[j].x - p[i].x,
                                        p[j].y - p[i].y);
                f[i].x += mag * direction.x / distance;
                f[j].x -= mag * direction.x / distance;
                f[i].y += mag * direction.y / distance;
                f[j].y -= mag * direction.y / distance;
            }
        }
    }

    public void moveBodies() {
        Point deltaV, deltaP;
        
        for (int i = 0; i < gnumBodies; i++) {
            deltaV = new Point( (f[i].x / ms[i]) * DT, 
                                (f[i].y / ms[i]) * DT);
            deltaP = new Point( ((v[i].x + deltaV.x) / 2) * DT,
                                ((v[i].y + deltaV.y) / 2) * DT);

            v[i].x += deltaV.x;
            v[i].y += deltaV.y;
            p[i].x += deltaP.x;
            p[i].y += deltaP.y;
            f[i].x = f[i].y = 0.0;
        }
    }

    /* Calculates the distance between two Bodys. */
    public double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    public static double randInterval(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static void main(String[] args) {

        final int MAX_BODIES = 240;
        final int MAX_STEPS = 400000;

        int gnumBodies, numSteps;
        long startTime, endTime;
        double massOfBodies;
        double massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1])
                : MAX_STEPS;
        numResultsShown = (args.length > 2) ? Integer.parseInt(args[2]) : 5;
        massOfBodies = (args.length > 3) ? Integer.parseInt(args[3]) : EARTH_MASS;
        massVariance = (args.length > 4) ? Integer.parseInt(args[4]) : 0.1;

        Nbody prg = new Nbody(gnumBodies, numSteps, massOfBodies, massVariance);

        System.out.println("\n- Initial Conditions -\n");
        Util.printArrays(prg.p, prg.v, gnumBodies, numResultsShown);

        startTime = System.nanoTime();

        // Start seq work
        for (int i = 0; i < numSteps; i++) {
            prg.calculateForces();
            prg.moveBodies();
        }
        // Finished seq work

        endTime = System.nanoTime() - startTime;

        System.out.println("\n- After simulation -\n");
        Util.printArrays(prg.p, prg.v, gnumBodies, numResultsShown);

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
    }
}