import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Fork extends Remote {
    public void pickUp(int philosopherId) throws RemoteException;
    public void putDown(int philosopherId) throws RemoteException;
    public void leave(int philosopherId) throws RemoteException;
}
