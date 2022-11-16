package fuel;

import java.util.Random;

/************************
     * Supplier
     * The vehicle threads calls the monitor methods
     * to request and release access to the
     * station in order to get or/and deposit fuel.
     ************************/
    public class Supplier implements Runnable {
        private static final int MAX_TRAVEL = 200; // maximum travel/wait (ms)
        private static final int MIN_TRAVEL = 50; // minimum travel/wait

        private Station station;
        private final int id;
        private int load;
        private int fuel;
        private int type;
        private Random rGen;

        public Supplier(Station station, int id, int load) {
            this.station = station;
            this.id = id;
            this.load = load;
            this.fuel = 20;
            this.type = id % 2;

            this.rGen = new Random();
        }

        public int rand(int l, int u) {
            return (rGen != null) ? l + rGen.nextInt(u - l) : 0;
        }

        public void run() {
            try {
                System.out.format("Launched <Supply Vehicle %d>%n", id);
                while (station.nVehicles > 0) {

                    // Deposit fuel
                    Thread.sleep(rand(MIN_TRAVEL * 4, MAX_TRAVEL * 2));
                    station.requestDeposit((type == 0) ? "Nitrogen" : "Quantum", load);
                    Thread.sleep(20);
                    station.deposit(id, (type == 0) ? "Nitrogen" : "Quantum", load);
                    Thread.sleep(20);
                    station.releaseDeposit();

                    // Request fuel
                    station.requestFuel(fuel);
                    station.refuel(id, "Supplier", fuel);
                    station.releaseFuel();
                    Thread.sleep(rand(MIN_TRAVEL * 4, MAX_TRAVEL * 2));

                    type = (type + 1) % 2; // Switch type for variety
                }
                System.out.format("Halting <Supply Vehicle %d>%n", id);
            } catch (InterruptedException e) {
                System.out.format("# Error on %d%n", id);
                Thread.currentThread().interrupt();
            }
        }
    }
