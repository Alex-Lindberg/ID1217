/**
 * From root dir.
 * Usage: java bear.Pot [beeCount]
 * 
 * Because eat and put have inverted conditions
 * the solution should be fair.
 * When the capacity is reached the bee inverts 
 * the conditional and triggers the bear, which
 * resets the honey-pot and triggers the bees.
 */
package honeybees;

public class Pot {

    public static final int MAX_BEES = 12;
    public static final int MAX_HONEY = 50;
    public final int honeyCapacity;
    private boolean full = false;

    /* Resources */
    private volatile int honey;

    public Pot(int capacity) {
        this.honey = 0;
        this.honeyCapacity = capacity;
    }

    /**
     * Method used by the Bear to consume all the honey.
     */
    public synchronized void eat() throws InterruptedException {
        while (!full)
            wait();
        System.out.println("Bear> *yawn* Snacc time :)");
        honey = 0;
        full = false;
        System.out.println("Bear> *yawn* sleepy");
        notifyAll();
    }

    /**
     * Method used by the Bees to produce one unit of honey.
     */
    public synchronized void put(int id) throws InterruptedException {
        while (full)
            wait();
        System.out.format("Bee %d> ~Bzz~ *spits honey*%n", id);
        this.honey++;
        if (this.honey >= honeyCapacity) {
            full = true;
            System.out.format("Bee %d> ~BzzZzZZZZz?!~%n", id);
            notifyAll();
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
            Bear bear = new Bear(pot);
            Thread bearThread = new Thread(bear);

            /* Create bees */
            for (int i = 0; i < beeCount; i++) {
                Bee bee = new Bee(pot, i + 1);
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
