/**
 * start rmiregistry
 * 
 * cd App
 * java hw.Serv
 */
package hw;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
        
public class Serv implements Hello {

    public String sayHello() {
        return "Hello, world!";
    }
        
    public String sayGoodbye() {
        return "Goodbye!";
    }
        
    public static void main(String[] args) {
        
        try {
            Serv obj = new Serv();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Hello", stub);

            System.err.println("Server ready");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
