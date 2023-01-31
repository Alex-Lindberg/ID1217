package phil_1;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Forks extends Remote {
    int getForks(int philNum) throws RemoteException;

    int returnForks(int philNum) throws RemoteException;
}
