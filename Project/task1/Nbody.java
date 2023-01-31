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
 *  @author Alex Lindberg
 * 
 */
package task1;

import java.util.Random;

public class Nbody {

    public static final double G = 6.67e-3;
    public static final int MAX_BODIES = 240;
    public static final int MAX_STEPS = 400000;
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
    Point[] bodies; // x, y coords
    Point[] vs; // x, y velocity
    Point[] fs; // x, y Forces
    double[] ms; // Mass
    Random rGen;

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

        this.bodies = new Point[gnumBodies];
        this.vs = new Point[gnumBodies];
        this.fs = new Point[gnumBodies];
        this.ms = new double[gnumBodies];
        this.rGen = new Random();

        this.bodies[0] = new Point(0,0);
        this.vs[0] = new Point(0,0);
        this.fs[0] = new Point(0,0);
        this.ms[0] = EARTH_MASS * 333.0;

        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            this.bodies[i] = getRandPos(this.bodies[0], RADIUS, MIN_DIST);
            // Random velocity between [-velBound, velBound]
            // Velocity orthogonal to the direction vector from the sun to current body
            double vx =  (this.bodies[0].x - this.bodies[i].x) * START_VEL;
            double vy =  -(this.bodies[0].y - this.bodies[i].x) * START_VEL;
            this.vs[i] = new Point(vx,vy);
            this.fs[i] = new Point(0, 0);
            // Mass with some varaince
            this.ms[i] = massBody * (1 + rand(-massVariance, massVariance));
        }
    }

    public double rand(double min, double max) {
        return this.rGen.nextDouble() * (max - min) + min;
    }

    private Point getRandPos(Point center, double radius, double minDist) {
        double r = radius * Math.sqrt(this.rGen.nextDouble()) + minDist; // distance
        double theta = this.rGen.nextDouble() * 2 * Math.PI; // direction
        double x = center.x + r * Math.cos(theta); // cartesian pos x
        double y = center.y + r * Math.sin(theta); // cartesian pos y
        return new Point(x,y);
    }

    public void calculateForces() {
        double distance, mag, dirX, dirY;

        for (int i = 0; i < gnumBodies; i++) {
            for (int j = i + 1; j < gnumBodies; j++) {
                distance = dist(bodies[i], bodies[j]);
                mag = (G * ms[i] * ms[j]) / (Math.pow(distance, 2) + SOFTENING);
                dirX = bodies[j].x - bodies[i].x;
                dirY = bodies[j].y - bodies[i].y;
                fs[i].x += mag * dirX / distance;
                fs[j].x -= mag * dirX / distance;
                fs[i].y += mag * dirY / distance;
                fs[j].y -= mag * dirY / distance;
            }
        }
    }

    public void moveBodies() {
        double dx, dy;
        double dvx, dvy;

        for (int i = 0; i < gnumBodies; i++) {
            dvx = (fs[i].x / ms[i]) * DT;
            dvy = (fs[i].y / ms[i]) * DT;

            dx = ((vs[i].x + dvx) / 2) * DT;
            dy = ((vs[i].y + dvy) / 2) * DT;

            vs[i].x += dvx;
            vs[i].y += dvy;

            bodies[i].x += dx;
            bodies[i].y += dy;

            fs[i].x = fs[i].y = 0.0;
        }
    }

    /* Calculates the distance between two Bodys. */
    public double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    /**
     * Sub-class for representation of a celestial body
     */
    private class Point {
        public double x;
        public double y;

        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {

        int gnumBodies, numSteps;
        long startTime, endTime;
        double massOfBodies;
        double massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1]) : MAX_STEPS;
        numResultsShown = (args.length > 2) ? Integer.parseInt(args[2]) : 5;
        massOfBodies = (args.length > 3) ? Integer.parseInt(args[3]) : EARTH_MASS;
        massVariance = (args.length > 4) ? Integer.parseInt(args[4]) : 0.1;

        Nbody prg = new Nbody(gnumBodies, numSteps, massOfBodies, massVariance);

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        System.out.println("Body  \t: x\ty\t| vx\tvy");
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                prg.vs[i].x,
                prg.vs[i].y);
        }
        System.out.format("Total Body count : %d%n", prg.gnumBodies);

        System.out.println("\n- After simulation -\n");
        startTime = System.nanoTime();
        // Start seq work

        for (int i = 0; i < numSteps; i++) {
            prg.calculateForces();
            prg.moveBodies();
        }

        // Finished seq work
        endTime = System.nanoTime() - startTime;

        // Printing end result
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                prg.vs[i].x,
                prg.vs[i].y);
        }

        System.out.format("%n- Simulation executed in %.1f ms -%n%n", endTime * Math.pow(10, -6));
    }
}