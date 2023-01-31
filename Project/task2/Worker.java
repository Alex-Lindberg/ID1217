package task2;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker implements Runnable {
    int id;
    int numSteps;
    Nbody work;
    CyclicBarrier barrier;

    public Worker(int w, int numSteps, Nbody work, CyclicBarrier barrier) {
        this.id = w;
        this.work = work;
        this.numSteps = numSteps;
        this.barrier = barrier;
    }

    public void barrier(int w) {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            System.err.format("Error: Exception caught for worker %d%n", w);
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < numSteps; i++) {
            work.calculateForces(id);
            barrier(id);
            work.moveBodies(id);
            barrier(id);
        }
    }
}
