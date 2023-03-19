/**
 * Nbody-Problem simulator
 * Sequential Barnes-Hut version
 * 
 * Time complexity: O(Nlog(N))
 *  
 * Usage (from root):
 *  javac task3/BarnesHutSimulation.java
 *  java task1.Nbody.java [gnumBodies] [numSteps] [numWorkers] [numResultsShown]
 * 
 * where:
 *  gnumBodies: The number of bodies used in the simulation.
 *  numSteps: The number of steps/cycles.
 *  numWorkers: The number of threads.
 *  numResultsShown: The number of results to be printed to stdout.
 * 
 * Some liberty when initializing the bodies was taken to make the 
 * results look nice when displayed as a figure.
 * 
 *  @author Alex Lindberg
 * 
 */
package task3;

import java.util.Random;

import util.Body;
import util.Constants;
import util.Util;

public class BarnesHutSimulation {

    public static final double EARTH_MASS = Constants.EARTH_MASS;
    public static final double SUN_MASS = Constants.SUN_MASS;
    public static final double RADIUS = Constants.RADIUS;
    public static final double MIN_DIST = Constants.MIN_DIST;
    public static final double START_VEL = Constants.START_VEL;
    public static final double MASS_VARIANCE = Constants.MASS_VARIANCE;
    public static final double DT = Constants.DT;

    public final int gnumBodies;
    public final double theta;

    Body[] bodies;
    BarnesHutTree tree;

    public BarnesHutSimulation(int gnumBodies, double theta) {
        this.gnumBodies = gnumBodies;
        this.theta = theta;

        this.bodies = new Body[gnumBodies];

        // The "Sun"
        double r, x1, x2, y1, y2, d, a, vx, vy, mass, theta0;
        x1 = 0;
        y1 = 0;
        this.bodies[0] = new Body(x1, y1, 0, 0, SUN_MASS, DT);

        Random rand = new Random();
        for (int i = 1; i < gnumBodies; i++) {
            r = (RADIUS - MIN_DIST) * rand.nextDouble() + MIN_DIST; // distance
            theta0 = rand.nextDouble() * 2 * Math.PI; // direction
            x2 = x1 + r * Math.sin(theta0); // cartesian pos x
            y2 = y1 + r * Math.cos(theta0); // cartesian pos y
            d = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            a = Math.atan2(y2 - y1, x2 - x1);
            vx = START_VEL * d * Math.sin(a);
            vy = -START_VEL * d * Math.cos(a);
            mass = EARTH_MASS * Math.max((1 + randInterval(-MASS_VARIANCE, MASS_VARIANCE)), 0.01);
            this.bodies[i] = new Body(x2, y2, vx, vy, mass, DT);
        }

        this.tree = buildTree(this.bodies);
    }

    public static double randInterval(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    private BarnesHutTree buildTree(Body[] bodies) {
        // Find the bounding box that contains all the bodies
        double xMin = Double.POSITIVE_INFINITY;
        double xMax = Double.NEGATIVE_INFINITY;
        double yMin = Double.POSITIVE_INFINITY;
        double yMax = Double.NEGATIVE_INFINITY;

        for (Body body : bodies) {
            if (body.x < xMin) {
                xMin = body.x;
            }
            if (body.x > xMax) {
                xMax = body.x;
            }
            if (body.y < yMin) {
                yMin = body.y;
            }
            if (body.y > yMax) {
                yMax = body.y;
            }
        }

        // Create the root node of the Barnes-Hut sub-tree
        double cx = (xMin + xMax) / 2;
        double cy = (yMin + yMax) / 2;
        double width = Math.max(xMax - xMin, yMax - yMin);
        BarnesHutTree root = new BarnesHutTree(cx, cy, width, this.theta);

        // Insert each body into the tree
        for (Body body : bodies) {
            root.insert(body);
        }

        return root;
    }

    private boolean shouldRerender = false;
    private boolean running = true;

    public void run(boolean[] config) throws InterruptedException {
        run(-1, config);
    }

    public void run(int numSteps, boolean[] config) throws InterruptedException {
        long t0, timeToBuild = 0, timeToUpdate = 0, timeToMove = 0;
        Thread renderer = new Thread(() -> {
            BarnesHutSimulationGUI g = new BarnesHutSimulationGUI(this, config[0], config[1]);
            try {
                while (running) {
                    if (shouldRerender) {
                        g.repaint();
                        shouldRerender = false;
                    }
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });
        renderer.start();

        for (int i = 0; i < numSteps || numSteps == -1; i++) {
            t0 = System.nanoTime();
            this.tree = buildTree(bodies);
            timeToBuild += System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (Body b : bodies) {
                this.tree.updateForce(b);
            }
            timeToUpdate += System.nanoTime() - t0;

            t0 = System.nanoTime();
            for (Body b : bodies) {
                b.move();
            }
            timeToMove += System.nanoTime() - t0;
            shouldRerender = true;
        }
        running = false;
        if (renderer != null)
            renderer.join();
        System.out.format("Sequential:%n   Build (n=%d):\t%,d,%n   update: \t\t%,d,%n   move: \t\t%,d%n%n",
                numSteps, timeToBuild, timeToUpdate, timeToMove);
        timeToBuild /= numSteps;
        timeToUpdate /= numSteps;
        timeToMove /= numSteps;
        System.out.format("Sequential AVGs:%n   Build (n=%d):\t%,d,%n   update: \t\t%,d,%n   move: \t\t%,d%n%n",
                numSteps, timeToBuild, timeToUpdate, timeToMove);
    }

    public static void main(String[] args) throws InterruptedException {
        final int MAX_BODIES = 240;
        final int MAX_STEPS = 350000;
        final double MAX_FAR = 2.0;

        int gnumBodies, numSteps;
        int numResultsShown = 5;
        double startTime, endTime;
        double far = 1.5;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1])
                : MAX_STEPS;
        far = (args.length > 2) && (Double.parseDouble(args[2]) < MAX_FAR) ? Double.parseDouble(args[2])
                : far;
        numResultsShown = (args.length > 3) && (Integer.parseInt(args[3]) < gnumBodies) ? Integer.parseInt(args[3])
                : numResultsShown;

        boolean showQuads = true;
        boolean showCenterOfMass = true;
        boolean[] config = new boolean[] { showQuads, showCenterOfMass };

        BarnesHutSimulation sim = new BarnesHutSimulation(gnumBodies, far);

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        Util.printArrays(sim.bodies, gnumBodies, numResultsShown);
        if (numSteps <= 0) {
            sim.run(config);
        } else {

            startTime = System.nanoTime();

            sim.run(numSteps, config);

            endTime = System.nanoTime() - startTime;

            // Printing end result
            System.out.format("\n- After %d steps -%n%n", numSteps);
            Util.printArrays(sim.bodies, gnumBodies, numResultsShown);

            System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
            System.out.println("---------------------------------");
            // System.out.format("%d & %.1f \\\\ %n", gnumBodies, endTime * Math.pow(10,
            // -9));
        }
    }
}
