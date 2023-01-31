package task4;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker implements Runnable {

    public final int id;
    private final int numWorkers;
    private final int numSteps;
    private final int gnumBodies;
    private Nbody work;
    private double far;
    private CyclicBarrier forceBarrier;
    private CyclicBarrier moveBarrier;

    public Worker(int id, Nbody work, double far, CyclicBarrier forceBarrier, CyclicBarrier moveBarrier) {
        this.id = id;
        this.work = work;
        this.numWorkers = work.numWorkers;
        this.numSteps = work.numSteps;
        this.gnumBodies = work.gnumBodies;
        this.far = far;
        this.forceBarrier = forceBarrier;
        this.moveBarrier = moveBarrier;
    }


    @Override
    public void run() {
        // for (int i = 0; i < numSteps; i++) {
        //     Quad quad = new Quad(0, 0, far);
        //     BHTree tree = new BHTree(quad);

        //     for (int j = 0; j < gnumBodies; j++) {
        //         if (work.bodies[j].in(quad)) 
        //             tree.insert(work.bodies[j]);
        //     }
        //     for (int j = id; j < gnumBodies; j += numWorkers) {
        //         tree.updateForce(work.bodies[j]);
        //     }
        //     barrier(id);

        //     for (int j = id; j < gnumBodies; j += numWorkers) {
        //         work.bodies[j].update();
        //     }
        //     barrier(id);
        // }
        try {
        for (int i = 0; i < numSteps; i++) {
            Quad q = new Quad(0, 0, far);
            BHTree tree = new BHTree(q);

            for (int j = 0; j < gnumBodies; j++) {
                if (work.bodies[j].in(q))
                    tree.insert(work.bodies[j]);
            }

            for (int j = id; j < gnumBodies; j += numWorkers) {
                tree.updateForce(work.bodies[j]);
            }
            forceBarrier.await();

            for (int j = id; j < gnumBodies; j += numWorkers) {
                work.bodies[j].update();
            }
            /* We need to wait again since a far-ahead thread may start
             * calculating forces without others having finished moving 
             * their assigned bodies. */
            moveBarrier.await();
        }
    } catch (InterruptedException | BrokenBarrierException e) {
        System.err.format("Error: Exception caught for worker %d%n", id);
        e.printStackTrace();
        Thread.currentThread().interrupt();
    }
    }
}
