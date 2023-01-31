package pairs;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Client
 */
public class Student implements Runnable {

    private static Request stub;
    private String name;
    private int id;

    public Student(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {

            if(!stub.requestGroup(String.valueOf(id), name))
                System.out.println("Failed to add student");

            while (true) {

                if(stub.groupReady()) {
                    String[] grp = stub.getGroup(String.valueOf(id));
                    System.out.format("Group: %s and %s", grp[0], grp[1]);
                    break;
                }

                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {

        // String host = (args.length < 1) ? null : args[0];
        try {
            String n = "";
            int id = -1;
            if (args.length > 0)
                n = args[0];
            if (args.length > 1)
                id = Integer.parseInt(args[1]);
            Registry registry = LocateRegistry.getRegistry(null);

            // Request
            stub = (Request) registry.lookup("teacher");
            if (n.equals("")) {
                n = String.valueOf(stub.getNumStudents());
            }
            if (id == -1) {
                n = String.valueOf(stub.getNumStudents());
            }
            Student std = new Student(n);
            Thread t = new Thread(std);
            t.start();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }

    
}
