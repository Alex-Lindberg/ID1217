package fuel;

/**
 * Station
 * 
 * Monitor: Shared cell
 * The monitor should be used to control access to and the use of the station
 */
public class Station {

    /* -- Variables -- */

    private static final int MAX_NITROGEN = 200; // N
    private static final int MAX_QUANTUM = 200; // Q

    private final int maxDocks; // Q
    public int nVehicles; // Number of vehicles
    public int nNitrogen; // Amount of nitrogen
    public int nQuantum; // Amount of quantum fluid

    private int nDocked; // Number of vehicle docking spaces
    private int nDepositing = 0;

    /* -- Constructor -- */

    public Station(int nDocked, int nVehicles) {
        this.maxDocks = nDocked;
        this.nDocked = 0;
        this.nVehicles = nVehicles;
        this.nNitrogen = 100;
        this.nQuantum = 100;
    }

    /* -- Monitor procedures -- */

    public synchronized void requestFuel(int cap) throws InterruptedException {
        while (nDepositing > 0 || maxDocks < nDocked || nNitrogen + nQuantum <= cap)
            wait();
        nDocked++;
    }

    public synchronized void releaseFuel() {
        nDocked--;
        if (nDocked < maxDocks)
            notifyAll();
    }

    public synchronized int refuel(int id, String v, int cap) throws InterruptedException {
        while (nNitrogen + nQuantum <= cap)
            wait();
        System.out.format("%s %d> refuling, Supply[N:%d, Q:%d]%n", v, id, nNitrogen, nQuantum);
        if (nNitrogen > nQuantum) {
            int partN = nNitrogen - cap;
            if (partN >= 0) {
                nNitrogen = partN;
            } else {
                nQuantum += partN;
                nNitrogen = 0;
            }
        } else {
            int partQ = nQuantum - cap;
            if (partQ >= 0) {
                nQuantum = partQ;
            } else {
                nNitrogen += partQ;
                nQuantum = 0;
            }
        }
        notify();
        if (nNitrogen < 0)
            System.out.format("# WARNING: (-) NITROGEN SUPPLY (%d)#%n", nNitrogen);
        if (nQuantum < 0)
            System.out.format("# WARNING: (-) QUANTUM SUPPLY (%d)#%n", nQuantum);
        return cap;
    }

    public synchronized void requestDeposit(String type, int cap) throws InterruptedException {
        // Check that we can empty the whole tank before commiting
        while (nDepositing > 0
                || (type.equals("Nitrogen") && (MAX_NITROGEN - nNitrogen) <= cap)
                || (type.equals("Quantum") && (MAX_QUANTUM - nQuantum) <= cap))
            wait();
        nDepositing++;
    }

    public synchronized void releaseDeposit() {
        nDepositing--;
        if (nDepositing == 0)
            notifyAll();
    }

    public synchronized void deposit(int id, String type, int cap) {
        if (type.equals("Nitrogen")) {
            nNitrogen += cap;
        } else {
            nQuantum += cap;
        }
        System.out.format("Supplier %d> Depositing, new Supply[N:%d, Q:%d]%n", id, nNitrogen, nQuantum);
    }

    public static void main(String[] args) throws InterruptedException {
        int numD = 2; // number of docking spaces
        int numV = 2; // number of vehicles
        int numSV = 1; // number of supply vehicles
        int numTrips = 5;

        /* Argument parsing */
        try {
            if (args.length > 0) numD = Integer.parseInt(args[0]);
            if (numD > 20) numD = 20;
            if (args.length > 1) numV = Integer.parseInt(args[1]);
            if (numV > 20) numV = 20;
            if (args.length > 2) numSV = Integer.parseInt(args[2]);
            if (numSV > 20) numSV = 20;
            if (args.length > 3) numTrips = Integer.parseInt(args[3]);
            if (numTrips > 50) numTrips = 50;
        } catch (NumberFormatException e) {
            System.out.println("USAGE: java fuel.Station [numVehicles] [trips] [numSuppliers]");
            System.exit(1);
        }

        /* Thread Creation */

        Station station = new Station(numD, numV);

        Thread[] vehicles = new Thread[numV];
        Thread[] suppliers = new Thread[numSV];
        for (int i = 0; i < vehicles.length; i++) {
            Vehicle v = new Vehicle(station, i, numTrips);
            vehicles[i] = new Thread(v);
            vehicles[i].start();
        }
        for (int i = 0; i < suppliers.length; i++) {
            Supplier v = new Supplier(station, i, 100);
            suppliers[i] = new Thread(v);
            suppliers[i].start();
        }

        /* Thread closing */
        for (Thread thread : suppliers)thread.join();
        for (Thread thread : vehicles) thread.join();
    }
}