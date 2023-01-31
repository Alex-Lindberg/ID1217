package pairs;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Request extends Remote {

    public boolean requestGroup(String id, String name) throws RemoteException;
    public boolean groupReady() throws RemoteException;
    public String[] getStudents() throws RemoteException;
    public int getNumStudents() throws RemoteException;

}
