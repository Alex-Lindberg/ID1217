package honeybees;

/* Producers */
public class Bee implements Runnable {
    public final int id;

    private Pot pot;

    public Bee(Pot pot, int id) {
        this.pot = pot;
        this.id = id;
    }

    public void run() {
        while (true) {
            try {
                pot.put(id);
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
