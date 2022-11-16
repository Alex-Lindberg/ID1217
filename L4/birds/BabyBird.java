package birds;

/**
 * Baby bird subclass.
 * Consumer.
 */
public class BabyBird implements Runnable {

    public final int id; /* Thread id */
    private Dish dish;

    public BabyBird(Dish dish, int id) {
        this.id = id;
        this.dish = dish;
    }

    /**
     * Consumes 1 unit from the worms pool. Returns a boolean on completion.
     * 
     * @param id The thread id
     * @return True if sucessful, otherwise false
     */
    public void run() {
        while (true) {
            try {
                dish.eat(this.id);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
