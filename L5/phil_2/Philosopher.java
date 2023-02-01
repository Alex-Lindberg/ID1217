import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Philosopher implements Runnable {
    private Fork butler;
    private int philosopherId;
    private int hunger = 5;

    public Philosopher(int philosopherId) {
        this.philosopherId = philosopherId;
        try {
            System.out.format("P-[%d]: \tFetching registry%n", philosopherId);
            Registry registry = LocateRegistry.getRegistry();
            System.out.format("P-[%d]: \tLocating Butler%n", philosopherId);
            butler = (Fork) registry.lookup("Butler");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.format("P-[%d]: \tRunning...%n", philosopherId);
            while (hunger > 0) {
                think();
                butler.pickUp(philosopherId);
                eat();
                butler.putDown(philosopherId);
            }
            System.out.format("P-[%d]: \tFinished%n", philosopherId);
            butler.leave(philosopherId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void think() {
        try {
            System.out.format("P-[%d]: \tThinking%n", philosopherId);
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void eat() {
        try {
            System.out.format("P-[%d]: \tEating%n", philosopherId);
            this.hunger--;
            Thread.sleep((long) (Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting...");
        int numPhilosophers = 5;
        Philosopher[] philosophers = new Philosopher[numPhilosophers];
    
        for (int i = 0; i < numPhilosophers; i++) {
            philosophers[i] = new Philosopher(i);
            new Thread(philosophers[i]).start();
        }
    }
    
}
