package birds;

/**
 * Parent bird subclass.
 * Handles refilling food, i.e. the producer.
 */
public class ParentBird implements Runnable {

    private Dish dish;

    ParentBird(Dish dish) {
        this.dish = dish;
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(100); /* simply to get more chirps */
                dish.fill();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}