/**
 * From root dir.
 * Usage: java birds.Nest [birdCount]
 */
package birds;

import java.util.concurrent.Semaphore;

public class Nest {

    private volatile int worms;
    private Semaphore full;
    private Semaphore empty;

    public static final int MAX_WORMS = 5;
    public static final int MAX_BIRDS = 10;

    public Nest() {
        this.worms = 5;

        /* fair=true to ensure no bird "starves" */
        this.full = new Semaphore(1, true);

        /* fair=false because it doesn't really matter, only parent aquires it */
        this.empty = new Semaphore(0, false);
    }

    /**
     * Parent bird subclass.
     * Handles refilling food, i.e. the producer.
     */
    public class ParentBird implements Runnable {

        public void run() {
            while (true) {
                try {
                    if (worms <= 0) {
                        empty.acquire();
                        Thread.sleep(50); /* simply to get more chirps */
                        worms += MAX_WORMS;
                        System.out.format("Parent> Refilled with %d worms%n", worms);
                        empty.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * Baby bird subclass.
     * Consumer.
     */
    public class BabyBird implements Runnable {

        public final int id; /* Thread id */
        private int consumed; /* Tracks food consumption. Used for debugging */

        public BabyBird(int id) {
            // super(name);
            this.id = id;
            this.consumed = 0;
        }

        /**
         * Consumes 1 unit from the worms pool. Returns a boolean on completion.
         * 
         * @param id The thread id
         * @return True if sucessful, otherwise false
         */
        public boolean eat() {
            if (worms > 0) {
                System.out.format("Bird %d> *slurp* nom (%d)%n", this.id, worms);
                worms--;
                return true;
            } else
                return false;
        }

        public void run() {
            while (true) {
                try {
                    full.acquire();
                    if (eat()) {
                        this.consumed++;
                        full.release();
                        Thread.sleep(50);
                    } else {
                        System.out.format("Bird %d: !!!*CHIRP*!!!%n", id);
                        full.release();
                        empty.release();
                        Thread.sleep(50);
                    }
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
            int birdCount = 1;
            try {
                if (args.length > 0)
                    birdCount = Integer.parseInt(args[0]);
                if (birdCount > MAX_BIRDS)
                    birdCount = MAX_BIRDS;
            } catch (NumberFormatException e) {
                System.out.println("USAGE: java birds.Nest [threadCount]");
                System.exit(1);
            }

            Nest scene = new Nest();

            ParentBird parent = scene.new ParentBird();
            Thread parentThread = new Thread(parent);
            parentThread.start();

            Thread[] children = new Thread[birdCount];

            for (int i = 0; i < birdCount; i++) {
                BabyBird bird = scene.new BabyBird(i + 1);
                children[i] = new Thread(bird);
                children[i].start();
            }
            for (Thread thread : children) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }

}
