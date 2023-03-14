package task4;

import java.util.Random;
import java.util.concurrent.CyclicBarrier;

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

    // private boolean[] config;

    public final int gnumBodies;
    public final double theta;

    Body[] bodies;
    BarnesHutTree tree;
    public static int currentStep = 0;

    public BarnesHutSimulation(int gnumBodies, double theta, boolean[] config) {
        this.gnumBodies = gnumBodies;
        this.theta = theta;
        // this.config = config;

        this.bodies = new Body[gnumBodies];

        // The "Sun"
        this.bodies[0] = new Body(0, 0, 0, 0, SUN_MASS, DT);

        Random rand = new Random();
        for (int i = 1; i < gnumBodies; i++) {

            double r = RADIUS * Math.sqrt(rand.nextDouble()) + MIN_DIST; // distance
            double theta0 = rand.nextDouble() * 2 * Math.PI; // direction
            double x = bodies[0].x + r * Math.cos(theta0); // cartesian pos x
            double y = bodies[0].y + r * Math.sin(theta0); // cartesian pos y

            double vx = (this.bodies[0].x - x) * START_VEL;
            double vy = -(this.bodies[0].y - y) * START_VEL;
            double mass = EARTH_MASS * (1 + randInterval(-MASS_VARIANCE * 1.1, MASS_VARIANCE));
            this.bodies[i] = new Body(x, y, vx, vy, mass, DT);
        }
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
        double width = Math.max(xMax - xMin, yMax - yMin) + 0.1;
        BarnesHutTree root = new BarnesHutTree(cx, cy, width, this.theta);

        // Insert each body into the tree
        for (Body body : bodies) {
            root.insert(body);
        }

        return root;
    }

    public Thread[] simulate(int numWorkers, int numSteps) {
        CyclicBarrier barrier = new CyclicBarrier(numWorkers);
        CyclicBarrier cycleBarrier = new CyclicBarrier(numWorkers, () -> {
            currentStep++;
            // Last to reach barrier re-builds the tree
            if (currentStep < numSteps) {
                this.tree = buildTree(this.bodies);
            }
        });

        this.tree = buildTree(this.bodies);

        Thread[] workers = new Thread[numWorkers];
        for (int w = 0; w < numWorkers; w++) {
            int id = w;
            workers[id] = new Thread(() -> {
                try {
                    // Partition bodies array into smaller chunks
                    int chunkSize = gnumBodies / numWorkers;
                    int start = chunkSize * id;
                    int end = (id == numWorkers - 1) ? gnumBodies : chunkSize * (id + 1);
                    while (currentStep < numSteps) {

                        // Update forces for the assigned chunk of bodies
                        for (int i = start; i < end; i++) {
                            tree.updateForce(bodies[i]);
                        }

                        barrier.await();

                        for (int i = start; i < end; i++) {
                            bodies[i].move();
                        }

                        cycleBarrier.await();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            workers[w].start();
        }
        return workers;
    }

    public static void main(String[] args) throws Exception {
        final int MAX_BODIES = 240;
        final int MAX_STEPS = 350000;
        final int MAX_WORKERS = 6;
        final Double MAX_FAR = 2.0;

        int gnumBodies, numSteps, numWorkers;
        int numResultsShown = 5;
        double startTime, endTime;
        double far = 1.5;

        /* Parameters */
        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1])
                : MAX_STEPS;
        far = (args.length > 2) && (Double.parseDouble(args[2]) < MAX_FAR) ? Double.parseDouble(args[2])
                : far;
        numWorkers = (args.length > 3) && (Integer.parseInt(args[3]) < MAX_WORKERS) ? Integer.parseInt(args[3])
                : MAX_WORKERS;
        numResultsShown = (args.length > 4) && (Integer.parseInt(args[4]) < gnumBodies) ? Integer.parseInt(args[4])
                : numResultsShown;

        
        boolean showQuads = true;
        boolean showCenterOfMass = false;
        boolean[] config = new boolean[] { showQuads, showCenterOfMass };

        /* Program start */

        BarnesHutSimulation sim = new BarnesHutSimulation(gnumBodies, far, config);
        Thread[] workers = new Thread[numWorkers];

        if (numResultsShown > 0) {
            System.out.println("\n- Initial Conditions -\n");
            Util.printArrays(sim.bodies, gnumBodies, numResultsShown);
        }

        startTime = System.nanoTime();
        
        /* Creating the threads */
        workers = sim.simulate(numWorkers, numSteps);
        for (Thread thread : workers) {
            thread.join();
        }
        endTime = System.nanoTime() - startTime;

        if (numResultsShown > 0) {
            System.out.format("\n- After %d steps -%n%n", numSteps);
            Util.printArrays(sim.bodies, gnumBodies, numResultsShown);
        }

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
        // System.out.format("%d & %.1f \\\\ %n", gnumBodies, endTime * Math.pow(10,-9));
    }
}
