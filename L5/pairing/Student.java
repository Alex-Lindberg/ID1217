package pairing;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Student implements Runnable {
    private String name;
    private PairingService teacher;

    public Student(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            teacher = (PairingService) registry.lookup("teacher");
            teacher.requestPartner(name);

            String partnerName = "";
            while (partnerName.equals("")) {
                partnerName = teacher.receivePartner(name);
                Thread.sleep(50);
            }
            System.out.format("%s \t: my partner is %s!%n", name, partnerName);
        } catch (RemoteException | NotBoundException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int numStudents = args.length > 0 ? Integer.parseInt(args[0]) : 9;
        try {
            for (int i = 1; i <= numStudents; i++) {
                Student student = new Student("Student " + i);
                new Thread(student).start();
            }
        } catch (Exception e) {
            System.out.println("Student exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
