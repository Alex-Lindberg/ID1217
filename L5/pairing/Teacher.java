package pairing;

import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

public class Teacher implements PairingService {
    private Queue<String> studentQueue;
    private Queue<String[]> studentPairs;
    private int pairsLeft;
    private int numStudents;

    public Teacher(int numStudents) throws RemoteException {
        studentQueue = new LinkedList<>();
        studentPairs = new LinkedList<>();
        this.pairsLeft = (int) Math.ceil(numStudents / 2);
        this.numStudents = numStudents;
    }

    @Override
    public synchronized void requestPartner(String studentName) throws RemoteException {
        studentQueue.offer(studentName);
        System.out.format("%s requests a partner%n", studentName);
        if (studentQueue.size() >= 2) {
            String student1 = studentQueue.poll();
            String student2 = studentQueue.poll();
            studentPairs.offer(new String[] { student1, student2 });
            System.out.format("%s gets paired with %s%n", student1, student2);
            pairsLeft--;
        } else if (numStudents % 2 == 1 && pairsLeft == 0) {
            String student = studentQueue.poll();
            studentPairs.offer(new String[] { student, student });
            System.out.format("%s is the odd one out%n", student);
            pairsLeft--;
        }
    }

    @Override
    public synchronized String receivePartner(String studentName) throws RemoteException {
        for (String[] pair : studentPairs) {
            if (pair[0].equals(studentName)) {
                numStudents--;
                
                return pair[1];
            }
            if (pair[1].equals(studentName)) {
                numStudents--;
                return pair[0];
            }
        }        
        return "";
    }


    public static void main(String[] args) {
        int numStudents = args.length > 0 ? Integer.parseInt(args[0]) : 9;
        try {
            Naming.unbind("teacher");
            System.out.println("Unbound teacher");
        } catch (Exception e) {
            System.out.println("No teacher found");
        }

        try {
            Teacher teacher = new Teacher(numStudents);
            PairingService stub = (PairingService) UnicastRemoteObject.exportObject(teacher, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind("teacher", stub);

            System.out.println("Teacher is ready and bound in the RMI registry.");
        } catch (RemoteException | AlreadyBoundException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

}
