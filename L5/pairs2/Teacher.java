package pairs2;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Teacher implements Serv, Runnable {

    public static ArrayList<String[]> groups = new ArrayList<>();
    private int numStudents = 0;
    private int maxStudents;
    private boolean groupReady = false;
    private Semaphore lockPair;

    int currentGroup = 0;
    String[] currentPair = new String[] { "", "", "", "" };

    public Teacher(int n) {
        this.maxStudents = n;
        lockPair = new Semaphore(1);
    }

    public synchronized int requestGroup(String id, String name) {
        try {
            lockPair.acquire();
        } catch (Exception e) {
        }
        numStudents++;
        if (currentPair[0].equals("")) {
            currentPair[0] = id;
            currentPair[1] = name;
            lockPair.release();
        } else if (currentPair[2].equals("")) {
            currentPair[2] = id;
            currentPair[3] = name;
            groupReady = true;
            createGroup();
            if (numStudents >= maxStudents) {
                for (String[] g : groups) {
                    System.out.format("Group: %s and %s%n", g[1], g[3]);
                }
                // break;
            }
        }
        return currentGroup;
    }

    public synchronized String[] getGroup(int id) {
        if (id < 0 || id >= groups.size())
            return new String[] { "" };
        return groups.get(id);
    }

    public synchronized int getNumStudents() {
        return numStudents;
    }

    private synchronized void createGroup() {
        groups.add(currentPair);
        currentGroup++;
        currentPair = new String[] { "", "", "", "" };
        groupReady = false;
        lockPair.release();
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (groupReady) {
                    createGroup();
                }
                if (numStudents >= maxStudents) {
                    for (String[] g : groups) {
                        System.out.format("Group: %s and %s%n", g[1], g[3]);
                    }
                    break;
                }
                Thread.sleep(20);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            // Get the number of students
            int numStudents = 3;
            if (args.length > 0)
                numStudents = Integer.parseInt(args[0]);
            if (numStudents > 20)
                numStudents = 20;
            System.out.format("Number of students: %d%n", numStudents);

            Teacher obj = new Teacher(numStudents);
            Serv stub = (Serv) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("teacher", stub);

            // Thread teach = new Thread(obj);
            // teach.start();
            // System.err.println("Server ready");

            // teach.join();

        } catch (NumberFormatException e) {
            System.err.println("Please enter the number of students as an argument");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

}
