package fuel;

import java.util.Random;

/************************
 * Vehicle
 * The vehicle threads calls the monitor methods
 * to request and release access to the
 * station in order to get or/and deposit fuel.
 ************************/
public class Vehicle implements Runnable {

    private static final int MAX_TRAVEL = 200; // maximum travel/wait (ms)
    private static final int MIN_TRAVEL = 50; // minimum travel/wait

    private Station station;
    private final int id;
    private int trips;
    private int fuel;

    private Random rGen;

    public Vehicle(Station station, int id, int trips) {
        this.station = station;
        this.id = id;
        this.trips = trips;
        this.fuel = 20;
        
        this.rGen = new Random();
    }

    public int rand(int l, int u) {
        return (rGen != null) ? l + rGen.nextInt(u - l) : 0;
    }

    public void run() {
        try {
            System.out.format("Launched <Vehicle %d>%n", id);
            while (trips > 0) {
                Thread.sleep(rand(MIN_TRAVEL, MAX_TRAVEL));

                station.requestFuel(fuel);
                Thread.sleep(20);
                station.refuel(id, "Vehicle", fuel);
                station.releaseFuel();

                Thread.sleep(rand(MIN_TRAVEL, MAX_TRAVEL));
                trips--;
                System.out.format("Vehicle %d> %d trips left%n", id, trips);
            }
            // Vehicle finished all their trips
            station.nVehicles--;
            System.out.format("Vehicle %d> Finished trips%n", id);
        } catch (InterruptedException e) {
            System.out.format("# Error on %d%n", id);
            Thread.currentThread().interrupt();
        }
    }
}

