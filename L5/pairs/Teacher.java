package pairs;

import java.util.ArrayList;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Teacher implements Request, Runnable{
    
    public static ArrayList<String[]> students = new ArrayList<>();
    public static ArrayList<String[]> groups = new ArrayList<>();
    private int maxStudents;
    private int numStudents = 0;
    private int studentsHandled = 0;

    public synchronized boolean requestGroup(String id, String name) {
        numStudents++;
        return students.add(new String[]{id,name});
    }

    public synchronized String[] getStudents() {
        String[] s = new String[students.size()];
        return students.toArray(s);
    }

    public synchronized int getNumStudents() {
        return numStudents;
    }

    public synchronized void createGroup() {
        if(students.size() > 1 && maxStudents - studentsHandled > 1) {
            String[] s1 = students.get(numStudents - 2);
            String[] s2 = students.get(numStudents - 1);
            String[] group = new String[]{s1[0], s1[1], s2[0],s2[1]};
            studentsHandled += 2;
            groups.add(group);
        }
        else {
            String[] s1 = students.get(numStudents - 1);
            String[] group = new String[]{s1[0], s1[1], s1[0], s1[1]};
            students.remove(0);
            studentsHandled += 1;
            groups.add(group);
            for (String[] g : groups) {
                System.out.format("Group: %s and %s%n", g[1], g[3]);
            }
        }
    }

    public Teacher(int n) {
        this.maxStudents = n;
    }

    @Override
    public void run() {
        try {
            while(true) {
                if (groupReady()) {
                    createGroup();
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
            int numStudents = 5;
            if (args.length > 0) numStudents = Integer.parseInt(args[0]);
            if (numStudents > 20) numStudents = 20;
            System.out.format("Number of students: %d%n", numStudents);
            
            Teacher obj = new Teacher(numStudents);
            Request stub = (Request) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("teacher", stub);

            
            Thread teach = new Thread(obj);
            teach.start();
            
            System.err.println("Server ready");

        } catch (NumberFormatException e) {
            System.err.println("Please enter the number of students as an argument");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }


}
