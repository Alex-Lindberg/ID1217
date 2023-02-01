import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Butler implements Fork {
    private boolean[] forks;
    private int atTable;

    public Butler(int numPhilosophers) {
        this.forks = new boolean[numPhilosophers];
        this.atTable = numPhilosophers;
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = true;
        }
    }

    @Override
    public synchronized void pickUp(int philosopherId) throws RemoteException {
        while (!forks[philosopherId] || !forks[(philosopherId + 1) % forks.length]) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.format("P-[%d]: \tGive fork%n", philosopherId);
        forks[philosopherId] = false;
        forks[(philosopherId + 1) % forks.length] = false;
    }

    @Override
    public synchronized void putDown(int philosopherId) throws RemoteException {
        System.out.format("P-[%d]: \tGet back fork%n", philosopherId);
        forks[philosopherId] = true;
        forks[(philosopherId + 1) % forks.length] = true;
        notifyAll();
    }

    @Override
    public void leave(int philosopherId) throws RemoteException {
        System.out.format("P-[%d]: \tLeaving Table%n", philosopherId);
        this.atTable--;
        if (this.atTable <= 0) {
            try {
                Naming.unbind("Butler");
                UnicastRemoteObject.unexportObject(this, true);
                System.out.println("Butler going home.");
            } catch (Exception e) {
                System.out.println("Failed to shut down butler.");
            }
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("Unbinding butler from registry...");
            Naming.unbind("Butler");
        } catch (Exception e) {
            System.out.println("No butler bound");
        }

        try {
            Butler butler = new Butler(5);
            Fork stub = (Fork) UnicastRemoteObject.exportObject(butler, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Butler", stub);
            System.out.println("Butler ready");
        } catch (Exception e) {
            System.err.println("Butler exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
