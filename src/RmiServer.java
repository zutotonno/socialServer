import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RmiServer implements Runnable {
	int PORT;
	public RmiServer(int rmiPort) {
		PORT=rmiPort;
	}

	@Override
	public void run() {
		try {
			GestSocial forum = (GestSocial) UnicastRemoteObject.exportObject(new GestSocialImpl(), 0);
			Registry registry = LocateRegistry.createRegistry(PORT);
			registry.rebind(GestSocial.REMOTE_OBJECT_NAME, forum);
			System.out.println("RMI server ready");
		} catch (Exception e) {
			System.out.println("Server error:" + e.getMessage());
		}

	}

}
