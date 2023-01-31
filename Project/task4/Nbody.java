/**
 * Nbody-Problem simulator
 * Parallel Barnes-Hut version
 * 
 * Time complexity: O(n log(n)))
 * 
 * Usage (from root):
 *  javac task4/Nbody.java
 *  java task4.Nbody.java [gnumBodies] [numSteps] [far] [numResultsShown] 
 *                        [massBody] [massVariance] [speedVariance]
 * 
 * where:
 *  gnumBodies:         The number of bodies used in the simulation.
 *  numSteps:           The number of steps/cycles.
 *  far:                The distance used to decide when to approximate 
 *                          (low values means faster but less accurate)
 *  numWorkers:         The number of worker threads.
 *  numResultsShown:    The number of results to be printed to stdout.
 *  massBody:           The mass used for each body.
 *  massVariance:       The mass used for each body.
 *  speedVariance:      Percentage variance in initialized speed for each body
 * 
 *  @author Alex Lindberg
 */
package task4;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class Nbody {

    public static final int MAX_BODIES = 240;
    public static final int MAX_STEPS = 100000;
    public static final Double EARTH_MASS = 59.742;
    public static final double RADIUS = 150;
    public static final double MIN_DIST = 80;
    public static final double START_VEL = 0.0008;

    public final int gnumBodies;
    public final int numSteps;
    public final double massBody;
    public final int numWorkers;

    Body[] bodies; // x, y coords
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
    public Nbody(int gnumBodies, int numSteps, int numWorkers, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.massBody = massBody;
        this.numWorkers = numWorkers;

        this.bodies = new Body[gnumBodies];
        this.rGen = new Random();

        this.bodies[0] = new Body(0, 0, 0, 0, 0, 0, massBody * 333);

        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            double[] pos = getRandPos(this.bodies[0].px, this.bodies[0].py, RADIUS, MIN_DIST);

            // Velocity orthogonal to the direction vector from the sun to current body
            double vx =  (this.bodies[0].px - pos[0]) * START_VEL;
            double vy =  -(this.bodies[0].py - pos[1]) * START_VEL;
            
            double mass = massBody * (1 + rand(1 - massVariance, 1 + massVariance));
            this.bodies[i] = new Body(pos[0], pos[1], vx, vy, 0, 0, mass);
        }
    }

    private double rand(double min, double max) {
        return this.rGen.nextDouble() * (max - min) + min;
    }

    private double[] getRandPos(double cx, double cy, double radius, double minDist) {
        double r = radius * Math.sqrt(this.rGen.nextDouble()) + minDist; // distance
        double theta = this.rGen.nextDouble() * 2 * Math.PI; // direction
        double x = cx + r * Math.cos(theta); // cartesian pos x
        double y = cy + r * Math.sin(theta); // cartesian pos y
        return new double[] { x, y };
    }

    public static void main(String[] args) {

        long startTime, endTime;
        int gnumBodies, numSteps;
        int numWorkers;
        double far;
        double massBody, massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1]) : MAX_STEPS;
        far = (args.length > 2) ? Integer.parseInt(args[2]) : RADIUS / 8;
        numWorkers = (args.length > 3) ? Integer.parseInt(args[3]) : 4;
        numResultsShown = (args.length > 4) ? Integer.parseInt(args[4]) : 5;
        massBody = (args.length > 5) ? Double.parseDouble(args[5]) : EARTH_MASS;
        massVariance = (args.length > 6) ? Integer.parseInt(args[6]) : 0.05;

        Thread[] workers = new Thread[numWorkers];
        CyclicBarrier barrier = new CyclicBarrier(numWorkers);
        CyclicBarrier b2 = new CyclicBarrier(numWorkers);
        Nbody prg = new Nbody(gnumBodies, numSteps, numWorkers, massBody, massVariance);

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        System.out.println("Body \t: x\ty\t| vx \tvy");
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f \t%.3f %n", i, prg.bodies[i].px, prg.bodies[i].py,
                    prg.bodies[i].vx,
                    prg.bodies[i].vy);
        }
        System.out.format("Total Body count : %d%n", gnumBodies);
        System.out.format("Total Worker count : %d%n", numWorkers);

        startTime = System.nanoTime();
        // Start parallel work

        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Thread(new Worker(i, prg, far, barrier, b2));
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
            System.out.format("Body %d\t: %.0f\t%.0f\t| %.3f \t%.3f %n", i, prg.bodies[i].px, prg.bodies[i].py,
                    prg.bodies[i].vx,
                    prg.bodies[i].vy);
        }
        System.out.format("%n- Simulation executed in %.3f ms -%n%n", endTime * Math.pow(10, -6));
    }
}