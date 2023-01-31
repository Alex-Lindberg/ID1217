package pairs2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Serv extends Remote {

    public int requestGroup(String id, String name) throws RemoteException;
    public String[] getGroup(int id) throws RemoteException;
    public int getNumStudents() throws RemoteException;

}
