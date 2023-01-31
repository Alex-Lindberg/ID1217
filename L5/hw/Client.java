/**
 * java  -classpath classDir example.hello.Client
 * 
 * cd App
 * java hw.Client
 */
package hw;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    private Client() {}

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {

            Registry registry = LocateRegistry.getRegistry(host);
            Hello stub = (Hello) registry.lookup("Hello");
            String response = stub.sayHello();
            System.out.println("response: " + response);

            Hello stub2 = (Hello) registry.lookup("Goodbye");
            String response2 = stub2.sayGoodbye();
            System.out.println("second: " + response2);
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
