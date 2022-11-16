/**
 * From root dir.
 * Usage: java bridge.Bridge [carCount] [tripCount]
 */
package bridge;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Bridge {

    public static final int MAX_CARS = 12;
    public static final int MAX_TRIPS = 20;

    public static final int MAX_SLEEP = 50;
    public static final int MIN_SLEEP = 10;

    private int nNorth = 0; /* Number of active northbound cars */
    private int nSouth = 0; /* Number of active soutbound cars */

    /**
     * @return timestamp with the format [seconds:milliseconds]
     */
    public synchronized String getTS() { return new SimpleDateFormat("[ss:SSS]").format(new Date()); }

    synchronized void northEnter(int id) throws InterruptedException {
        while (nSouth > 0) {
            System.out.format("%s Car %d> Waiting to go North%n", getTS(), id);
            wait();
        }
        ++nNorth;
        System.out.format("%s Car %d> Going North%n", getTS(), id);
    }

    synchronized void northExit(int id) {
        --nNorth;
        System.out.format("%s Car %d> Exiting North%n", getTS(), id);
        if (nNorth == 0)
            notifyAll();
    }

    synchronized void southEnter(int id) throws InterruptedException {
        while (nNorth > 0) {
            System.out.format("%s Car %d> Waiting to go South%n", getTS(), id);
            wait();
        }
        ++nSouth;
        System.out.format("%s Car %d> Going South%n", getTS(), id);
    }

    synchronized void southExit(int id) {
        --nSouth;
        System.out.format("%s Car %d> Exiting South%n", getTS(), id);
        if (nSouth == 0)
            notifyAll();
    }

    public static void main(String[] args) {
        try {
            int tripCount = 3;
            int northCars = 3;
            int southCars= 3;
            try {
                if (args.length > 0) tripCount = Integer.parseInt(args[0]);
                if (tripCount > MAX_TRIPS) tripCount = MAX_TRIPS;
                if (args.length > 1) northCars = Integer.parseInt(args[1]);
                if (northCars > MAX_CARS) northCars = MAX_CARS;
                if (args.length > 2) southCars = Integer.parseInt(args[2]);
                if (southCars > MAX_CARS) southCars = MAX_CARS;
            } catch (NumberFormatException e) {
                System.out.println("USAGE: java bear.Pot [carCount]");
                System.exit(1);
            }

            Bridge bridge = new Bridge();

            /* Init car pool */
            Thread[] nc = new Thread[northCars];
            Thread[] sc = new Thread[southCars];

            /* Create south cars */
            for (int i = 0; i < northCars; i++) {
                Car car = new Car(bridge, i, 0, tripCount);
                nc[i] = new Thread(car);
                nc[i].start();
            }
            /* Create north cars */
            for (int i = 0; i < southCars; i++) {
                Car car = new Car(bridge, i + northCars, 1, tripCount);
                sc[i] = new Thread(car);
                sc[i].start();
            }

            for (Thread thread : nc) thread.join();
            for (Thread thread : sc) thread.join();

        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.exit(1);
        }
    }
}

