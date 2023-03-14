package task4;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class BarnesHutSimulation {

    public static final double DOWNSCALING = Constants.DOWNSCALING;
    public static final double EARTH_MASS = Constants.EARTH_MASS;
    public static final double SUN_MASS = Constants.SUN_MASS;
    public static final double RADIUS = Constants.RADIUS;
    public static final double MIN_DIST = Constants.MIN_DIST;
    public static final double START_VEL = Constants.START_VEL;
    public static final double MASS_VARIANCE = Constants.MASS_VARIANCE;

    private boolean[] config;

    public final int gnumBodies;
    public final double theta;
    public final double DT;

    Body[] bodies;
    static BarnesHutTree tree;

    public BarnesHutSimulation(int gnumBodies, double theta, double dt, boolean[] config) {
        this.gnumBodies = gnumBodies;
        this.theta = theta;
        this.DT = dt;
        this.config = config;

        this.bodies = new Body[gnumBodies];

        // The "Sun"
        this.bodies[0] = new Body(0, 0, 0, 0, SUN_MASS, dt);

        Random rand = new Random();
        for (int i = 1; i < gnumBodies; i++) {

            double r = RADIUS * Math.sqrt(rand.nextDouble()) + MIN_DIST; // distance
            double theta0 = rand.nextDouble() * 2 * Math.PI; // direction
            double x = bodies[0].x + r * Math.cos(theta0); // cartesian pos x
            double y = bodies[0].y + r * Math.sin(theta0); // cartesian pos y

            double vx = (this.bodies[0].x - x) * START_VEL;
            double vy = -(this.bodies[0].y - y) * START_VEL;
            double mass = EARTH_MASS * (1 + randInterval(
                    -MASS_VARIANCE,
                    MASS_VARIANCE)) * DOWNSCALING;
            this.bodies[i] = new Body(x, y, vx, vy, mass, dt);
        }

        tree = buildTree(this.bodies);
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

    // public void run(boolean shouldRun) {
    // boolean showQuads = config[0];
    // boolean showCenterOfMass = config[1];
    // BarnesHutSimulationGUI GUI = new BarnesHutSimulationGUI(this, showQuads,
    // showCenterOfMass);
    // while (shouldRun) {
    // this.tree = buildTree(bodies);
    // for (Body b : bodies)
    // this.tree.updateForce(b);
    // for (Body b : bodies)
    // b.move();
    // if (shouldRun)
    // GUI.repaint();
    // }
    // }

    public static int currentStep = 0;

    public Thread[] simulate(int numWorkers, int numSteps) {
        CyclicBarrier barrier = new CyclicBarrier(numWorkers);
        CyclicBarrier exitBarrier = new CyclicBarrier(numWorkers, () -> {
            currentStep++;
        });
        Semaphore criticalWork = new Semaphore(1);

        Thread[] workers = new Thread[numWorkers];
        int span = Math.floorDiv(gnumBodies, numWorkers);
        for (int w = 0; w < numWorkers; w++) {
            int id = w;
            workers[id] = new Thread(() -> {
                int timesBuiltTree = 0;
                long t0, timeToBuild = 0, timeToUpdate = 0, timeToMove = 0;
                long timeAtBarrierA = 0, timeAtBarrierB = 0;
                long timeAtExitBarrier = 0;
                try {
                    int intervalEnd = (span * id) + span;
                    while (currentStep < numSteps) {

                        // First to arrive builds the tree
                        if (criticalWork.tryAcquire()) {
                            t0 = System.nanoTime();
                            tree = buildTree(bodies);
                            timeToBuild += System.nanoTime() - t0;
                            timesBuiltTree++;
                            criticalWork.release();
                        }
                        t0 = System.nanoTime();
                        barrier.await();
                        timeAtBarrierA += t0  / numSteps;
                        t0 = System.nanoTime();

                        for (int i = (span * id); i < intervalEnd; i++) {
                            tree.updateForce(bodies[i]);
                        }
                        timeToUpdate += System.nanoTime() - t0;

                        t0 = System.nanoTime();
                        barrier.await();
                        timeAtBarrierB += t0  / numSteps;
                        t0 = System.nanoTime();
                        
                        for (int i = (span * id); i < intervalEnd; i++) {
                            bodies[i].move();
                        }
                        timeToMove += System.nanoTime() - t0;
                        
                        t0 = System.nanoTime();
                        exitBarrier.await();
                        timeAtExitBarrier += t0  / numSteps;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.format("Worker: %d, %n   Build (n=%d):\t%,d,%n   update: \t\t%,d,%n   move: \t\t%,d%n%n",
                        id, timesBuiltTree, timeToBuild, timeToUpdate, timeToMove);
                timeToBuild /= Math.max(timesBuiltTree, 1);
                timeToUpdate /= numSteps;
                timeToMove /= numSteps;
                System.out.format("WorkerAVGS: %d, %n   Build (n=%d):\t%,d,%n   update: \t\t%,d,%n   move: \t\t%,d%n   timeAtBarrierA: \t%,d,%n   timeAtBarrierB: \t%,d,%n   timeAtBarrierExit: \t%,d,%n%n",
                        id, timesBuiltTree, timeToBuild, timeToUpdate, timeToMove, timeAtBarrierA, timeAtBarrierB, timeAtExitBarrier);
            });

            workers[w].start();
        }
        return workers;
    }

    public static void main(String[] args) throws Exception {
        final int MAX_BODIES = 240;
        final int MAX_STEPS = 350000;
        final int MAX_WORKERS = 4;
        final Double MAX_FAR = 2.0;

        int gnumBodies, numSteps, numWorkers;
        int numResultsShown = 0;
        double startTime, endTime;
        double dt = 0.1;
        double far = 1;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1])
                : MAX_STEPS;
        far = (args.length > 2) && (Double.parseDouble(args[2]) < MAX_FAR) ? Double.parseDouble(args[2])
                : MAX_FAR;
        numWorkers = (args.length > 3) && (Integer.parseInt(args[3]) < MAX_WORKERS) ? Integer.parseInt(args[3])
                : MAX_WORKERS;

        boolean showQuads = true;
        boolean showCenterOfMass = false;
        boolean[] config = new boolean[] { showQuads, showCenterOfMass };

        BarnesHutSimulation sim = new BarnesHutSimulation(gnumBodies, far, dt, config);
        Thread[] workers = new Thread[numWorkers];

        if(numResultsShown > 0) {
            System.out.println("\n- Initial Conditions -\n");
            Util.printArrays(sim.bodies, gnumBodies, numResultsShown);
        }
        
        startTime = System.nanoTime();
        
        workers = sim.simulate(numWorkers, numSteps);
        for (Thread thread : workers) {
            thread.join();
        }
        
        endTime = System.nanoTime() - startTime;
        
        if(numResultsShown > 0) {
            System.out.format("\n- After %d steps -%n%n", numSteps);
            Util.printArrays(sim.bodies, gnumBodies, numResultsShown);
        }

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
        // System.out.format("%d  & %.1f \\\\ %n", gnumBodies, endTime * Math.pow(10, -9));

    }
}
