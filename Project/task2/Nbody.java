/**
 * Nbody-Problem simulator
 * Parallel brute force version
 * 
 * Time complexity: O(N^2)
 * 
 * Usage (from root):
 *  javac task1/Nbody.java
 *  java task1.Nbody.java [gnumBodies] [numSteps] [numWorkers] [numResultsShown] 
 *                        [massBody] [massVariance] [speedVariance]
 * 
 * where parameters are:
 *  gnumBodies:         The number of bodies used in the simulation.
 *  numSteps:           The number of steps/cycles.
 *  numWorkers:         The number of worker threads.
 *  numResultsShown:    The number of results to be printed to stdout.
 *  massBody:           The mass used for each body.
 *  massVariance:       Percentage variance in initialized mass for each body
 *  speedVariance:      Percentage variance in initialized speed for each body
 * 
 *  All parameters are optional.
 * 
 *  @author Alex Lindberg
 * 
 */
package task2;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

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

    public static int prgCounter = 0;
    public static synchronized void increment() { prgCounter++; }
    // public static Runnable barrierAction =
    //    new Runnable() { public void run() { increment(); }};

    public final int gnumBodies;
    public final int numSteps;
    public final int numWorkers;

    // Data oriented solution, takes advantage of spatial locality better than
    // if we keep everything in a "body" class
    Point[] bodies; // x, y coords
    Point[] vs; // x, y velocity
    Point[][] fs; // x, y Forces
    double[] ms; // Mass
    Random rGen;

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies   The number of bodies
     * @param numSteps     The number of calculation steps/cycles
     * @param massBody     The mass of every body
     * @param massVariance The variance of mass between bodies
     * @param vel          Velocity bounds of a body (see init. loop for more info).
     */
    public Nbody(int gnumBodies, int numSteps, int numWorkers, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.numWorkers = numWorkers;
        this.bodies = new Point[gnumBodies];
        this.vs = new Point[gnumBodies];
        this.fs = new Point[numWorkers][gnumBodies];
        this.ms = new double[gnumBodies];
        this.rGen = new Random();

        // The sun
        this.bodies[0] = new Point(0,0);
        this.vs[0] = new Point(0,0);
        this.ms[0] = EARTH_MASS * 333;

        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            this.bodies[i] = getRandPos(this.bodies[0], RADIUS, MIN_DIST);

            // Velocity orthogonal to the direction vector from the sun to current body
            double vx =  (this.bodies[0].x - this.bodies[i].x) * START_VEL;
            double vy =  -(this.bodies[0].y - this.bodies[i].x) * START_VEL;
            this.vs[i] = new Point(vx,vy);
            // Mass with some varaince
            this.ms[i] = massBody * (1 + rand(-massVariance, massVariance));
        }
        for (int w = 0; w < numWorkers; w++) {
            for (int i = 0; i < gnumBodies; i++)
                this.fs[w][i] = new Point(0, 0);
        }
    }

    public double rand(double min, double max) {
        if(min == max) return 0.0;
        return this.rGen.nextDouble() * (max - min) + min;
    }

    private Point getRandPos(Point center, double radius, double minDist) {
        double r = radius * Math.sqrt(this.rGen.nextDouble()) + minDist; // distance
        double theta = this.rGen.nextDouble() * 2 * Math.PI; // direction
        double x = center.x + r * Math.cos(theta); // cartesian pos x
        double y = center.y + r * Math.sin(theta); // cartesian pos y
        return new Point(x,y);
    }

    public void calculateForces(int worker) {
        double distance, mag, dirX, dirY;

        // Reverse stripes allocation
        for (int i = worker; i < gnumBodies; i += numWorkers) {
            for (int j = i + 1; j < gnumBodies; j++) {
                distance = dist(bodies[i], bodies[j]);
                mag = (G * ms[i] * ms[j]) / (Math.pow(distance, 2) + SOFTENING);
                dirX = bodies[j].x - bodies[i].x;
                dirY = bodies[j].y - bodies[i].y;
                fs[worker][i].x += mag * dirX / distance;
                fs[worker][j].x -= mag * dirX / distance;
                fs[worker][i].y += mag * dirY / distance;
                fs[worker][j].y -= mag * dirY / distance;
            }
        }
    }

    public void moveBodies(int worker) {
        double dx, dy;
        double dvx, dvy;
        Point force = new Point(0.0, 0.0);

        for (int i = worker; i < gnumBodies; i += numWorkers) {
            for (int j = 0; j < numWorkers; j++) {
                force.x += fs[j][i].x;
                fs[j][i].x = 0.0;
                force.y += fs[j][i].y;
                fs[j][i].y = 0.0;
            }
            dvx = (force.x / ms[i]) * DT;
            dvy = (force.y / ms[i]) * DT;
            dx = (vs[i].x + dvx / 2) * DT;
            dy = (vs[i].y + dvy / 2) * DT;

            vs[i].x += dvx;
            vs[i].y += dvy;

            bodies[i].x += dx;
            bodies[i].y += dy;

            force.x = force.y = 0.0;
        }
    }

    /* Calculates the distance between two Bodies. */
    public double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    /**
     * Sub-class for representation of a celestial body
     */
    public class Point {
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
        double massBody;
        double massVariance;

        int numWorkers;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1]) : MAX_STEPS;
        numWorkers = (args.length > 2) ? Integer.parseInt(args[2]) : 4;
        numResultsShown = (args.length > 3) ? Integer.parseInt(args[3]) : 5;
        massBody = (args.length > 4) ? Double.parseDouble(args[4]) : EARTH_MASS;
        massVariance = (args.length > 5) ? Integer.parseInt(args[5]) : 0.1;

        Nbody prg = new Nbody(gnumBodies, numSteps, numWorkers, massBody, massVariance);
        Thread[] workers = new Thread[numWorkers];
        CyclicBarrier barrier = new CyclicBarrier(numWorkers, new Runnable() { public void run() { increment(); }});

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        System.out.println("Body  \t: x\ty\t| vx\tvy");
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                    prg.vs[i].x,
                    prg.vs[i].y);
        }
        System.out.format("Total Body count : %d%n", prg.gnumBodies);
        System.out.format("Total Worker count : %d%n", prg.numWorkers);
        

        startTime = System.nanoTime();

        // Start parallel work
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Thread(new Worker(i, numSteps, prg, barrier));
            workers[i].start();
        }
        for (int i = 0; i < numWorkers; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        // Finished parallel work
        endTime = System.nanoTime() - startTime;

        System.out.format("%n- After simulation (%d steps) -%n%n", numSteps);
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                prg.vs[i].x,
                prg.vs[i].y);
        }

        // Printing end time
        System.out.format("%n- Simulation executed in %.3f ms -%n%n", endTime * Math.pow(10, -6));
        System.out.format("- Counter = %d -%n", prgCounter);
    }
}
