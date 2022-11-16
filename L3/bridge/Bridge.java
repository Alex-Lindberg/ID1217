/**
 * From root dir.
 * Usage: java bridge.Bridge [carCount] [tripCount]
 */
package bridge;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Bridge {

    public static final int MAX_CARS = 12;
    public static final int MAX_TRIPS = 20;

    public static final int MAX_SLEEP = 50;
    public static final int MIN_SLEEP = 10;

    private Semaphore cross; /* Critical lock */
    private Semaphore northLock; /* delaying northbound cars */
    private Semaphore southLock; /* delaying southbound cars */

    private int nNorth = 0; /* Number of active northbound cars */
    private int nSouth = 0; /* Number of active soutbound cars */
    private int dNorth = 0; /* Number of delayed northbound cars */
    private int dSouth = 0; /* Number of delayed soutbound cars */

    public Bridge() {
        cross = new Semaphore(1);
        northLock = new Semaphore(0);
        southLock = new Semaphore(0);
    }

    public class Car implements Runnable {

        public final int id;
        private int trips;
        private boolean crossed;
        private Random r;

        public Car(int id, int trips) {
            this.id = id;
            this.trips = trips;
            this.crossed = false;
            this.r = new Random();
        }

        private boolean getDir(int id, boolean crossed) {
            return (id % 2 == 0) ? crossed : !crossed;
        }

        public void crossBridge(int id, int trips) throws InterruptedException {
            if (this.crossed) {
                this.trips--;
                this.crossed = !this.crossed;
            } else
                this.crossed = !this.crossed;
            System.out.format("Car %d> Crossing %s with %d trips left%n", id + 1,
                    getDir(id, crossed) ? "South" : "North", this.trips);
            Thread.sleep(MIN_SLEEP + (long) r.nextInt(MAX_SLEEP - MIN_SLEEP));
        }

        public void run() {
            while (trips > 0) {
                try {

                    /* Southbound */
                    if (getDir(id, crossed)) {
                        cross.acquire();
                        if (nNorth > 0 || dNorth > 0) {
                            dSouth++;
                            cross.release();
                            southLock.acquire();
                        }
                        nSouth++;
                        if (dSouth > 0) {
                            dSouth--;
                            southLock.release();
                        } else
                            cross.release();

                        /* Critical section */

                        crossBridge(id, trips);

                        cross.acquire();
                        nSouth--;
                        if (nSouth == 0 && dNorth > 0) {
                            dNorth--;
                            northLock.release();
                        } else
                            cross.release();
                    }
                    /* Northbound */
                    else {
                        cross.acquire();
                        if (nSouth > 0 || nNorth > 0) {
                            dNorth++;
                            cross.release();
                            northLock.acquire();
                        }
                        nNorth++;
                        cross.release();

                        /* Critical section */

                        crossBridge(id, trips);

                        cross.acquire();
                        nNorth--;
                        if (dNorth > 0) {
                            dNorth--;
                            northLock.release();
                        } else if (dSouth > 0) {
                            dSouth--;
                            southLock.release();
                        } else
                            cross.release();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

    }

    public static void main(String[] args) {
        try {
            int carCount = 1;
            int tripCount = 3;
            try {
                if (args.length > 0)
                    carCount = Integer.parseInt(args[0]);
                if (carCount > MAX_CARS)
                    carCount = MAX_CARS;
                if (args.length > 1)
                    tripCount = Integer.parseInt(args[1]);
                if (tripCount > MAX_TRIPS)
                    tripCount = MAX_TRIPS;
            } catch (NumberFormatException e) {
                System.out.println("USAGE: java bear.Pot [carCount]");
                System.exit(1);
            }

            Bridge bridge = new Bridge();

            /* Init car pool */
            Thread[] children = new Thread[carCount];

            /* Create cars */
            for (int i = 0; i < carCount; i++) {
                Car car = bridge.new Car(i, tripCount);
                children[i] = new Thread(car);
                children[i].start();
            }

            for (Thread thread : children) {
                thread.join();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
