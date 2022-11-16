/**
 * From root dir.
 * Usage: java bear.Pot [beeCount]
 */
package bear;

import java.util.concurrent.Semaphore;

public class Pot {

    public static final int MAX_BEES = 12;
    public static final int MAX_HONEY = 50;
    public final int honeyCapacity;

    private Semaphore wake; /* pot is full, bear wakes up */
    private Semaphore producing; /* pot is producing */

    /* Resources */
    private volatile int honey;

    public Pot(int capacity) {
        this.honey = 0;
        this.honeyCapacity = capacity;
        this.wake = new Semaphore(0, true);
        this.producing = new Semaphore(1, true);
    }

    /* Consumer */
    public class Bear implements Runnable {
        public void run() {
            while (true) {
                try {
                    wake.acquire();
                    System.out.println("Bear> *yawn* Snacc time :)");
                    honey = 0;
                    System.out.println("Bear> *yawn* sleepy");
                    wake.release();
                    producing.release();

                    if (wake.availablePermits() > 0)
                        wake.drainPermits();
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /* Producers */
    public class HoneyBee implements Runnable {
        public final int id;

        public HoneyBee(int id) {
            this.id = id;
        }

        public void run() {
            while (true) {
                try {
                    producing.acquire();
                    if (honey < honeyCapacity) {
                        System.out.format("Bee %d> ~Bzz~ *spits honey*%n", this.id);
                        honey++;
                        producing.release();
                    } else {
                        wake.release();
                        System.out.format("Bee %d> ~BzzZzZZZZz?!~%n", this.id);
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /* //////////////////// */
    /* \\\\\\\\\\\\\\\\\\\\ */
    public static void main(String[] args) {
        try {
            int beeCount = 1;
            int honeyCap = 10;
            try {
                if (args.length > 0)
                    beeCount = Integer.parseInt(args[0]);
                if (beeCount > MAX_BEES)
                    beeCount = MAX_BEES;
                if (args.length > 1)
                    honeyCap = Integer.parseInt(args[1]);
                if (honeyCap > MAX_HONEY)
                    honeyCap = MAX_HONEY;
            } catch (NumberFormatException e) {
                System.out.println("USAGE: java bear.Pot [beeCount]");
                System.exit(1);
            }

            Pot pot = new Pot(honeyCap);

            /* Init bee pool */
            Thread[] children = new Thread[beeCount];

            /* Create bear */
            Bear bear = pot.new Bear();
            Thread bearThread = new Thread(bear);

            /* Create bees */
            for (int i = 0; i < beeCount; i++) {
                HoneyBee bee = pot.new HoneyBee(i + 1);
                children[i] = new Thread(bee);
                children[i].start();
            }
            bearThread.start();

            for (Thread thread : children) {
                thread.join();
            }
            bearThread.join();

        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}
