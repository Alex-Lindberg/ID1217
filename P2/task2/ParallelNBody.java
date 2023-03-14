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
 * on my personal computer. (14887,8 m median)
 * 
 * Some liberty was taken to make the results look nice when displayed as a figure.
 * 
 *  @author Alex Lindberg
 * 
 */
package task2;

import util.Constants;
import util.Util;
import util.Util.*;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ParallelNBody {

    public static final double G = Constants.G;
    public static final double EARTH_MASS = Constants.EARTH_MASS;
    public static final double SUN_MASS = Constants.SUN_MASS;
    public static final double RADIUS = Constants.RADIUS;
    public static final double MIN_DIST = Constants.MIN_DIST;
    public static final double SOFTENING = Constants.SOFTENING;
    public static final double DT = Constants.DT;
    public static final double START_VEL = Constants.START_VEL;
    public static final double MASS_VARIANCE = Constants.MASS_VARIANCE;

    public final int gnumBodies;
    public final double massBody;
    public final int numSteps;
    
    public int currentStep = 0;
    private CyclicBarrier barrier;
    private CyclicBarrier barrier2;
    private Worker[] workers;
    private final int PR;

    // Data oriented variable storage
    Point[] p; // x, y coords
    Point[] v; // x, y velocity
    Point[][] f; // x, y Forces
    double[] m; // Mass

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies   The number of bodies
     * @param numSteps     The number of calculation steps/cycles
     * @param massOfBodies The mass of every body
     * @param massVariance The variance of mass between bodies
     * @param vel          Velocity bounds of a body (see init. loop for more info).
     */
    public ParallelNBody(int gnumBodies, int numSteps, int numWorkers, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.massBody = massBody;
        this.PR = numWorkers;

        this.p = new Point[gnumBodies];
        this.v = new Point[gnumBodies];
        this.f = new Point[numWorkers][gnumBodies];
        this.m = new double[gnumBodies];

        // Barrier, action increments step counter
        barrier = new CyclicBarrier(numWorkers);
        barrier2 = new CyclicBarrier(numWorkers, () -> { currentStep++; });
        // Prepare parallel workers
        workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++)
            workers[i] = new Worker(i);

        this.p[0] = new Point(0, 0);
        this.v[0] = new Point(0, 0);
        this.m[0] = SUN_MASS;

        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            this.p[i] = Point.getRandPos(this.p[0], RADIUS, MIN_DIST);

            // Velocity orthogonal to the direction vector from the sun to current body
            double vx = (this.p[0].x - this.p[i].x) * START_VEL;
            double vy = -(this.p[0].y - this.p[i].y) * START_VEL;
            this.v[i] = new Point(vx, vy);
            
            // Mass with some varaince
            this.m[i] = EARTH_MASS * (
                1 + randInterval(-(MASS_VARIANCE * 1.1), MASS_VARIANCE));
        }
        for (int w = 0; w < numWorkers; w++) 
            for (int i = 0; i < gnumBodies; i++) 
                this.f[w][i] = new Point(0, 0);
    }

    public void calculateForces(int w) {
        double distance, magnitude;
        Point direction;

        for (int i = w; i < gnumBodies; i += PR) {
            for (int j = i + 1; j < gnumBodies; j++) {
                distance = dist(p[i], p[j]);
                magnitude = (G * m[i] * m[j]) / ((distance * distance + SOFTENING * SOFTENING));
                direction = new Point(  p[j].x - p[i].x,
                                        p[j].y - p[i].y);
                f[w][i].x += magnitude * direction.x / distance;
                f[w][j].x -= magnitude * direction.x / distance;
                f[w][i].y += magnitude * direction.y / distance;
                f[w][j].y -= magnitude * direction.y / distance;
            }
        }
    }

    public void moveBodies(int w) {
        Point deltaV, deltaP;
        Point force = new Point(0, 0);

        for (int i = w; i < gnumBodies; i += PR) {
            for (int k = 0; k < PR; k++) {
                force.x += f[k][i].x; f[k][i].x = 0;
                force.y += f[k][i].y; f[k][i].y = 0;
            }
            deltaV = new Point( (force.x / m[i]) * DT, 
                                (force.y / m[i]) * DT);
            deltaP = new Point( (v[i].x + deltaV.x / 2) * DT,
                                (v[i].y + deltaV.y / 2) * DT);

            v[i].x += deltaV.x;
            v[i].y += deltaV.y;
            p[i].x += deltaP.x;
            p[i].y += deltaP.y;
            force.x = force.y = 0.0;
            
        }
    }

    /* Calculates the distance between two Bodys. */
    public double dist(Point p1, Point p2) {
        return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
    }

    public static double randInterval(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    private class Worker implements Runnable {
        int id;
        boolean finished = false;
    
        public Worker(int id) {
            this.id = id;
        }
        public void exitBarrier() throws Exception {
            barrier2.await(); 
            // The main thread increments the counter so we need only wait to finish
            if( currentStep >= numSteps)
                this.finished = true;
        }
        @Override
        public void run() {
            try {  
                while(!finished) {
                    calculateForces(this.id);
                    barrier.await();
                    moveBodies(this.id);
                    this.exitBarrier();
                }
            } catch (Exception e) {
                System.err.format("Error: Exception caught for worker %d%n", this.id);
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        final int MAX_BODIES = 240;
        final int MAX_STEPS = 350000;
        final int MAX_WORKERS = 4;

        int gnumBodies, numSteps, numWorkers;
        long startTime, endTime;
        double massOfBodies;
        double massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1])
                : MAX_STEPS;
        numWorkers = (args.length > 2) && (Integer.parseInt(args[2]) < MAX_WORKERS) ? Integer.parseInt(args[2])
                : MAX_WORKERS;
        numResultsShown = (args.length > 3) ? Integer.parseInt(args[3]) : 5;
        massOfBodies = (args.length > 4) ? Integer.parseInt(args[4]) : EARTH_MASS;
        massVariance = (args.length > 5) ? Integer.parseInt(args[5]) : 0.1;

        ParallelNBody prg = new ParallelNBody(gnumBodies, numSteps, numWorkers, massOfBodies, massVariance);
        Thread[] workerThreads = new Thread[numWorkers];

        System.out.println("\n- Initial Conditions -\n");
        Util.printArrays(prg.p, prg.v, gnumBodies, numResultsShown);

        startTime = System.nanoTime();

        // Start seq work

        for (int i = 0; i < numWorkers; i++) {
            workerThreads[i] = new Thread(prg.workers[i]);
            workerThreads[i].start();
        }
        for (int i = 0; i < numWorkers; i++) 
            workerThreads[i].join();

        // Finished seq work

        endTime = System.nanoTime() - startTime;

        System.out.format("\n- After %d steps -%n%n", numSteps);
        Util.printArrays(prg.p, prg.v, gnumBodies, numResultsShown);

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
        System.out.format("%d  & %.1f \\\\ %n", gnumBodies, endTime * Math.pow(10, -9));
    }
}