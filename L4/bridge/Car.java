package bridge;

import java.util.Random;

public class Car implements Runnable {

    public static final int MAX_CARS = 12;
    public static final int MAX_TRIPS = 20;

    public static final int MAX_SLEEP = 200;
    public static final int MIN_SLEEP = 80;

    private Bridge bridge;
    public final int id;
    private int dir;
    private int trips;
    private boolean crossed;
    private Random r;

    public Car(Bridge bridge, int id, int dir, int trips) {
        this.bridge = bridge;
        this.id = id;
        this.dir = dir;
        this.trips = trips;
        this.crossed = false;
        this.r = new Random();
    }

    public int rand(int l, int u) {
        return (r != null) ? l + r.nextInt(u - l) : 0;
    }

    private boolean getDir() {
        return (this.dir % 2 == 0) ? this.crossed : !this.crossed;
    }

    public void crossBridge() throws InterruptedException {
        System.out.format("%s Car %d> Crossing %s with %d trips left%n", bridge.getTS(), id,
                getDir() ? "North" : "South", this.trips);
        if (this.crossed) {
            this.trips--;
            this.crossed = !this.crossed;
        } else
            this.crossed = !this.crossed;
        Thread.sleep(rand(MIN_SLEEP, MAX_SLEEP));
    }

    public void run() {
        while (trips > 0) {
            try {

                if(getDir()) { // North
                    bridge.northEnter(id);
                    crossBridge();
                    bridge.northExit(id);
                }
                else {
                    bridge.southEnter(id);
                    crossBridge();
                    bridge.southExit(id);
                }
                Thread.sleep(rand(MIN_SLEEP/4, MAX_SLEEP/4));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

}
