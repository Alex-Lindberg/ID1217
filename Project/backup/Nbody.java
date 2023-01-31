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
 *  @author Alex Lindberg
 * 
 */
package backup;

import java.util.Random;

public class Nbody {

    public static final double G = 6.67e-11;
    public static final int MAX_BODIES = 240;
    public static final int MAX_STEPS = 300000;
    public static final int MAX_MASS = 10;
    public static final double DT = 1;

    public final int gnumBodies;
    public final int numSteps;
    public final double massBody;

    Body[] bodies;
    Random rGen;

    /**
     * Simulation of the nbody-problem
     * 
     * @param gnumBodies    The number of bodies
     * @param numSteps      The number of calculation steps/cycles
     * @param massOfBodies  The mass of every body
     * @param massVariance  The variance of mass between bodies
     * @param vel           Velocity bounds of a body (see init. loop for more info).
     */
    public Nbody(int gnumBodies, int numSteps, double massBody, double massVariance, double velBound) {
        this.gnumBodies = gnumBodies;
        this.numSteps = numSteps;
        this.massBody = massBody;

        bodies = new Body[gnumBodies];
        this.rGen = new Random();

        /*
        Random positions in x and y,
        Random velocity between [-velBound, velBound]
        No forces
        Mass with some varaince
        */
        for (int i = 0; i < gnumBodies; i++) {
            bodies[i] = new Body(
                (10 * (i % (int) Math.sqrt(gnumBodies))) + rGen.nextDouble() * 7,
                10 * (i / Math.sqrt(gnumBodies)) + rGen.nextDouble() * 7,
                rand(-velBound, velBound),
                rand(-velBound, velBound),
                0, 0,
                massBody * (1 + rand(1 - massVariance, 1 + massVariance)));
        }
    }

    public double rand(double min, double max) {
        return this.rGen.nextDouble() * ((max - min) + 1.0) + min;
    }


    public void calculateForces() {
        double distance, magnitude, dirX, dirY;

        for (int i = 0; i < gnumBodies; i++) {
            for (int j = i + 1; j < gnumBodies; j++) {
                distance = bodies[i].dist(bodies[j]);
                magnitude = (G * bodies[i].mass * bodies[j].mass) / (distance * distance);
                dirX = bodies[j].x - bodies[i].x;
                dirY = bodies[j].y - bodies[i].y;
                bodies[i].fx = bodies[i].fx + magnitude * dirX / distance;
                bodies[j].fx = bodies[j].fx - magnitude * dirX / distance;
                bodies[i].fy = bodies[i].fy + magnitude * dirY / distance;
                bodies[j].fy = bodies[j].fy - magnitude * dirY / distance;
            }
        }
    }

    public void moveBodies() {

        double dx, dy;
        double dvx, dvy;

        for (int i = 0; i < gnumBodies; i++) {
            dvx = (bodies[i].fx / bodies[i].mass) * DT;
            dvy = (bodies[i].fy / bodies[i].mass) * DT;

            dx = ((bodies[i].vx + dvx) / 2) * DT;
            dy = ((bodies[i].vy + dvy) / 2) * DT;

            bodies[i].vx = bodies[i].vx + dvx;
            bodies[i].vy = bodies[i].vy + dvy;

            bodies[i].x = bodies[i].x + dx;
            bodies[i].y = bodies[i].y + dy;

            bodies[i].fx = 0;
            bodies[i].fy = 0;
        }
    }

    /**
     * Sub-class for representation of a celestial body
     */
    private class Body {

        public double x;
        public double y;
        public double vx;
        public double vy;
        public double fx;
        public double fy;
        public double mass;

        public Body(double x, double y, double vx, double vy, double fx, double fy, double mass) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.fx = fx;
            this.fy = fy;
            this.mass = mass;
        }

        /* Calculates the distance between two Bodys. */
        public double dist(Body p) {
            return Math.sqrt(Math.pow((this.x - p.x), 2) + Math.pow((this.y - p.y), 2));
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
        massOfBodies = (args.length > 2) ? Integer.parseInt(args[2]) : MAX_MASS;
        massVariance = (args.length > 3) ? Integer.parseInt(args[3]) : 0.1;
        numResultsShown = (args.length > 4) ? Integer.parseInt(args[4]) : 5;

        Nbody prg = new Nbody(gnumBodies, numSteps, massOfBodies, massVariance, 0.001);

        // Printing starting conditions
        System.out.println("\n- Initial Conditions -\n");
        System.out.println("Body  \t:\tx\ty\t|\tvx\tvy");
        for (int i = 0; i < numResultsShown; i++) {
            System.out.format("Body %d\t:\t%.3f\t%.3f\t|\t%.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                    prg.bodies[i].vx,
                    prg.bodies[i].vy);
        }

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
            System.out.format("Body %d\t:\t%.3f\t%.3f\t|\t%.3f\t%.3f %n", i, prg.bodies[i].x, prg.bodies[i].y,
                    prg.bodies[i].vx,
                    prg.bodies[i].vy);
        }

        System.out.format("%n- Simulation executed in %.3f ms -%n%n", endTime * Math.pow(10, -6));
    }
}