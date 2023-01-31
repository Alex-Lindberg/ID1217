package phil_1;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Semaphore;

public class ForkServer implements Forks {
    // Access control semaphores for 5 forks
    static Semaphore[] s = { new Semaphore(1),
            new Semaphore(1),
            new Semaphore(1),
            new Semaphore(1),
            new Semaphore(1) };
    // Critical section of random()
    static Semaphore rs = new Semaphore(1);
    /* Array elements of forks show which philosopher holds the given fork */
    private int[] forks = { -1, -1, -1, -1, -1 };

    public int getForks(int philNum) {
        if (philNum < 0 || philNum > 4)
            return -1; // wrong argument
        System.out.println("Philosopher " + philNum + " trying to get forks...");
        while (true) {
            /* 1: Throw a coin selecting which fork to get first */
            try { // we don't want concurrent access to random number
                // generator
                // because of the risk of deadlock
                rs.acquire();
            } catch (InterruptedException e) {
            }
            int rnd = (int) (Math.random() * 2);
            rs.release();
            /* 2: If taken GOTO 1 else try to take the other fork */

            int fork1 = (philNum + rnd) % 5;
            // reserve right to access the data on the first selected fork
            try {
                s[fork1].acquire();
            } catch (InterruptedException e) {
            }
            if (forks[fork1] == -1) {
                int fork2 = (philNum + (1 - rnd)) % 5;
                /* 3: If the other fork taken GOTO 1 */
                // reserve right to access the data on the second selected fork
                try {
                    s[fork2].acquire();
                } catch (InterruptedException e) {
                }
                if (forks[fork2] == -1) {
                    forks[fork1] = philNum;
                    forks[fork2] = philNum;
                    System.out.println("Philosopher " + philNum +
                            " can start to eat using forks " +
                            fork1 + " and " + fork2 + "...");
                    s[fork1].release();
                    s[fork2].release();
                    return 0;
                }
                s[fork2].release();
                /* 4: If taken go to 1 */
            }
            s[fork1].release();
        }
    }

    public int returnForks(int philNum) {
        if (philNum < 0 || philNum > 4)
            return -1; // wrong argument
        int fork1 = philNum;
        int fork2 = (philNum + 1) % 5;
        try {
            s[fork1].acquire();
            s[fork2].acquire();
        } catch (InterruptedException e) {
        }

        System.out.println("Philosopher " + philNum + " has returned the forks " + fork1 + " and " + fork2 + "...");
        forks[fork1] = -1;
        forks[fork2] = -1;
        s[fork1].release();
        s[fork2].release();
        return 0;
    }

    public static void main(String[] args) {
        try {
            String name = "Forks";
            Forks server = new ForkServer();
            Forks stub = (Forks) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("ForkServer bound");
        } catch (Exception e) {
            System.err.println("ForkServer exception");
            e.printStackTrace();
        }
    }
}
