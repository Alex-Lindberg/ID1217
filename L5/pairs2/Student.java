package pairs2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client
 */
public class Student implements Runnable {

    private static Serv stub;
    private String name;
    private int id;

    public Student(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {

            int gid = stub.requestGroup(String.valueOf(id), name);
            String[] group = new String[]{""};

            while (group[0].equals("")) {
                group = stub.getGroup(gid);
                Thread.sleep(50);
            }
            System.out.format("Group: %s %s and %s %s%n", group[0], group[1], group[2], group[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {

        // String host = (args.length < 1) ? null : args[0];
        try {
            System.out.println(args.length);
            String n = "";
            int id = -1;
            n = (args.length > 0) ? args[0] : "Test";
            if (args.length > 1)
                id = Integer.parseInt(args[1]);
            Registry registry = LocateRegistry.getRegistry(null);

            // Request
            stub = (Serv) registry.lookup("teacher");
            
            if (id == -1) {
                n = String.valueOf(stub.getNumStudents());
            }
            Student std = new Student(n);
            Thread t = new Thread(std);
            t.start();
            t.join();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }

    
}
