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
package task3;

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

    private Body[] bodies; // x, y coords

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies   The number of bodies
     * @param numSteps     The number of calculation steps/cycles
     * @param massOfBodies The mass of every body
     * @param massVariance The variance of mass between bodies
     * @param vel          Velocity bounds of a body (see init. loop for more info).
     */
    public Nbody(int gnumBodies, int numSteps, double far, double massBody, double massVariance) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.far = far;
        this.massBody = massBody;

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
    }

    public Body[] getBodies() {
        return this.bodies;
    }

    private double randInterval(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public void simulate() {
        for (int i = 0; i < numSteps; i++) {
            Quad q = new Quad(new Point(0, 0), far);
            BHTree tree = new BHTree(q);

            // Build tree
            for (int j = 0; j < gnumBodies; j++) {
                if (bodies[j].in(q))
                    tree.insert(bodies[j]);
            }

            // Update force vectors
            for (int j = 0; j < gnumBodies; j++) {
                tree.updateForce(bodies[j]);
            }

            // Move bodies
            for (int j = 0; j < gnumBodies; j++) {
                bodies[j].moveBody();
            }
        }
    }

    public static void main(String[] args) {

        final int MAX_BODIES = 240;
        final int MAX_STEPS = 400000;

        long startTime, endTime;
        int gnumBodies, numSteps;
        double far;
        double massBody, massVariance;
        int numResultsShown;

        gnumBodies = (args.length > 0) && (Integer.parseInt(args[0]) < MAX_BODIES) ? Integer.parseInt(args[0])
                : MAX_BODIES;
        numSteps = (args.length > 1) && (Integer.parseInt(args[1]) < MAX_STEPS) ? Integer.parseInt(args[1]) : MAX_STEPS;
        far = (args.length > 2) ? Double.parseDouble(args[2]) : RADIUS / 8;
        numResultsShown = (args.length > 3) ? Integer.parseInt(args[3]) : 5;
        massBody = (args.length > 4) ? Double.parseDouble(args[4]) : EARTH_MASS;
        massVariance = (args.length > 5) ? Integer.parseInt(args[5]) : 0.05;

        Nbody prg = new Nbody(gnumBodies, numSteps, far, massBody, massVariance);

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        Util.printArrays(prg.getBodies(), gnumBodies, numResultsShown);

        startTime = System.nanoTime();
        // Start seq work

        prg.simulate();

        // Finished seq work
        endTime = System.nanoTime() - startTime;

        // Printing end result
        System.out.format("\n- After %d steps -%n%n", numSteps);
        Util.printArrays(prg.bodies, gnumBodies, numResultsShown);

        System.out.format("%n- Simulation executed in %.1f ms -%n", endTime * Math.pow(10, -6));
        System.out.println("---------------------------------");
    }
}