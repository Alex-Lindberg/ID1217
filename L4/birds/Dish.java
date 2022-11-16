/**
 * From root dir.
 * Usage: java birds.Birds [birdCount]
 * 
 * The solution should be mostly *fair*, maybe favor
 * the consumers (using eat) slightly.
 * 
 * Eat simply checks whether we can decrement worms
 * or not, but we never need to wait for another process.
 * 
 * fill requires us to wait for worms to be empty, but it's
 * never held up explicitly by another thread.
 */
package birds;

public class Dish {

    public volatile int worms;
    private boolean empty = false;

    public static final int MAX_WORMS = 5;
    public static final int MAX_BIRDS = 10;

    public Dish() {
        this.worms = 5;
    }
    /**
     * Simulates birds eating from a shared dish. 
     * 
     * @param id    ID of the bird 
     */
    public synchronized void eat(int id) {
        if (empty) {
            System.out.format("Bird %d: !!!*CHIRP*!!!%n", id);
            notifyAll();
        } else {
            this.worms--;
            System.out.format("Bird %d> *slurp* nom (%d)%n", id, worms);
            if (worms == 0)
                empty = true;
        }
    }

    public synchronized void fill() throws InterruptedException {
        while (!empty)
            wait();
        this.worms += MAX_WORMS;
        System.out.format("Parent> Refilled with %d worms%n", worms);
        empty = false;
    }

    /* ////////////////////// */
    /* \\\\\\\\ MAIN \\\\\\\\ */
    public static void main(String[] args) {
        try {
            int birdCount = 1;
            try {
                if (args.length > 0)
                    birdCount = Integer.parseInt(args[0]);
                if (birdCount > MAX_BIRDS)
                    birdCount = MAX_BIRDS;
            } catch (NumberFormatException e) {
                System.out.println("USAGE: java birds.Birds [threadCount]");
                System.exit(1);
            }

            Dish dish = new Dish();

            ParentBird parent = new ParentBird(dish);
            Thread parentThread = new Thread(parent);
            parentThread.start();

            Thread[] children = new Thread[birdCount];

            for (int i = 0; i < birdCount; i++) {
                BabyBird bird = new BabyBird(dish, i + 1);
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
