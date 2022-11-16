package honeybees;

/* Consumer */
public class Bear implements Runnable {

    private Pot pot;

    public Bear(Pot pot) { this.pot = pot; }

    public void run() {
        while (true) {
            try {
                pot.eat();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
