package pairing;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PairingService extends Remote {
  public void requestPartner(String studentName) throws RemoteException;
  public String receivePartner(String studentName) throws RemoteException;;
}

