/**
 * Nbody-Problem simulator
 * Sequential Barnes-Hut version
 * 
 * Time complexity: O(n log(n)))
 * 
 * Usage (from root):
 *  javac task3/Nbody.java
 *  java task3.Nbody.java [gnumBodies] [numSteps] [far] [numResultsShown] 
 *                        [massBody] [massVariance] [speedVariance]
 * 
 * where:
 *  gnumBodies:         The number of bodies used in the simulation.
 *  numSteps:           The number of steps/cycles.
 *  far:                The distance used to decide when to approximate 
 *                          (low values means faster but less accurate)
 *  numResultsShown:    The number of results to be printed to stdout.
 *  massBody:           The mass used for each body.
 *  massVariance:       The mass used for each body.
 *  speedVariance:      Percentage variance in initialized speed for each body
 * 
 *  @author Alex Lindberg
 */
package task4;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import util.Body;
import util.Quad;
import util.Util;
import util.Util.Point;

public class Nbody {

    public static final Double EARTH_MASS = 59.742;
    public static final double RADIUS = 150;
    public static final double MIN_DIST = 80;
    public static final double START_VEL = 0.0008;

    public final int gnumBodies;
    public final int numSteps;
    public final double far;
    public final double massBody;
    
    public int currentStep = 0;
    private CyclicBarrier treeBarrier;
    private CyclicBarrier moveBarrier;
    private Worker[] workers;
    private final int PR;

    private Body[] bodies; // x, y coords
    private BHTree tree;

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies   The number of bodies
     * @param numSteps     The number of calculation steps/cycles
     * @param massOfBodies The mass of every body
     * @param massVariance The variance of mass between bodies
     * @param vel          Velocity bounds of a body (see init. loop for more info).
     */
    public Nbody(int gnumBodies, int numSteps, int numWorkers, double far, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.far = far;
        this.massBody = massBody;
        this.PR = numWorkers;

        this.bodies = new Body[gnumBodies];
        this.bodies[0] = new Body(new Point(0, 0), new Point(0, 0), massBody * 333);

        Point p, v;
        double mass;
        for (int i = 1; i < gnumBodies; i++) {
            // Random position
            p = Point.getRandPos(this.bodies[0].p, RADIUS, MIN_DIST);

            // Velocity orthogonal to the direction vector from the sun to current body
            v = new Point(
                    (this.bodies[0].v.x - p.x) * START_VEL,
                    (this.bodies[0].v.y - p.y) * START_VEL);
            mass = massBody * (1 + randInterval(1 - massVariance, 1 + massVariance));
            this.bodies[i] = new Body(p, v, mass);
        }

        this.moveBarrier = new CyclicBarrier(numWorkers, () -> { this.currentStep++; });
        this.treeBarrier = new CyclicBarrier(numWorkers, () -> { makeTree(); });
        workers = new Worker[numWorkers];
        for (int i = 0; i < numWorkers; i++)
            workers[i] = new Worker(i);
    }

    public Body[] getBodies() {
        return this.bodies;
    }

    private double randInterval(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public void makeTree() {
        // Build tree
        Quad q = new Quad(new Point(0, 0), far);
        this.tree = new BHTree(q);
        for (int j = 0; j < gnumBodies; j++) {
            if (bodies[j].in(q))
                tree.insert(bodies[j]);
        }
    }

    public void simulate(int w) {
        try {
            this.treeBarrier.await();
                
            // Update force vectors
            for (int j = w; j < gnumBodies; j += PR) {
                tree.updateForce(bodies[j]);
            }

            this.moveBarrier.await();

            // Move bodies
            for (int j = w; j < gnumBodies; j += PR) {
                bodies[j].moveBody();
            } 
            workers[w].isFinished();

        } catch (InterruptedException | BrokenBarrierException e) {
            System.out.println("Thread " + w + " is interrupted");
            e.printStackTrace();
        }
        
    }

    private class Worker implements Runnable {
        int id;
        boolean finished = false;
    
        public Worker(int id) {
            this.id = id;
        }
        public void isFinished() {
            // The last thread increments the counter so we need only wait to finish
            if( currentStep >= numSteps)
                this.finished = true;
        }
        @Override
        public void run() {
            while(!finished) {
                simulate(id);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        final int MAX_BODIES = 240;
        final int MAX_STEPS = 400000;
        final int MAX_WORKERS = 4;

        long startTime, endTime;
        int gnumBodies, numSteps, numWorkers;
        double far;
        double massBody, massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1]) : MAX_STEPS;
        numWorkers = (args.length > 2) && (Integer.parseInt(args[2]) < MAX_WORKERS) ? Integer.parseInt(args[2])
                : MAX_WORKERS;
        far = (args.length > 3) ? Double.parseDouble(args[3]) : 1-5;
        numResultsShown = (args.length > 4) ? Integer.parseInt(args[4]) : 5;
        massBody = (args.length > 5) ? Double.parseDouble(args[5]) : EARTH_MASS;
        massVariance = (args.length > 6) ? Integer.parseInt(args[6]) : 0.05;

        Nbody prg = new Nbody(gnumBodies, numSteps, numWorkers, far, massBody, massVariance);
        Thread[] workerThreads = new Thread[numWorkers];

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        Util.printArrays(prg.getBodies(), gnumBodies, numResultsShown);

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

        // Printing end result
        System.out.format("\n- After %d steps -%n%n", numSteps);
        Util.printArrays(prg.bodies, gnumBodies, numResultsShown);

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
    }
}