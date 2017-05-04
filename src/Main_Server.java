import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main_Server {
	static int UDP;
	static int MULTICAST_PORT;
	final static int BLOCK_SIZE = 64;
	static String MC_GROUP ;
	static int PORT,RmiPort;
	static String HOST;
	static ArrayList<User> users;
	static File myFile,myFile2;
	static CopyOnWriteArrayList<PendingFriend> pendingRequest;
	static ArrayList<String> online;
	static long pending_duration;
	public static void main(String[] args) {
		try (ServerSocket server = new ServerSocket()) {
			setup();
			System.out.println(InetAddress.getLocalHost());
			server.bind(new InetSocketAddress(HOST, PORT));
			ExecutorService s_handlers = Executors.newCachedThreadPool();
			System.out.println("Server Ready");
			myFile = new File("list.ser");
			if (!myFile.exists()) {
				myFile.createNewFile();
				System.out.println("Created");
			}
			myFile2 = new File("pending.ser");
			if (!myFile2.exists()) {
				myFile2.createNewFile();
				System.out.println("Created");
			}
			loadUsers();
			Runnable saver = new File_Saver(myFile,users,myFile2,pendingRequest,pending_duration);
			Runnable sender = new Keep_Alive_Sender(users,online);
			Runnable rmiServer = new RmiServer(RmiPort);
			s_handlers.submit(saver);
			s_handlers.submit(sender);
			s_handlers.submit(rmiServer);
			while (true) {
				Socket client = server.accept();
				Runnable handler = new Client_Handler(client, users,pendingRequest,online);
				s_handlers.submit(handler);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void setup() throws IOException {
		Properties prop = new Properties();
		InputStream reader = new FileInputStream("PROP.xml");
		prop.loadFromXML(reader);
		UDP =Integer.parseInt(prop.getProperty("UDP"));
		MC_GROUP = prop.getProperty("MC_GROUP");
		MULTICAST_PORT = Integer.parseInt(prop.getProperty("multicast_port"));
		PORT = Integer.parseInt(prop.getProperty("portServer"));
		RmiPort = Integer.parseInt(prop.getProperty("RmiPort"));
		HOST = prop.getProperty("host");
		users = new ArrayList<User>();
		pendingRequest = new CopyOnWriteArrayList<PendingFriend>();// Collections.synchronizedSet(new
		online = new ArrayList<String>();
		pending_duration=Integer.parseInt(prop.getProperty("pending_duration"));
	}

	private static void loadUsers() throws IOException, ClassNotFoundException {
		synchronized (myFile) {
			FileInputStream fileIn;
			ObjectInputStream in2;
			try {
				fileIn = new FileInputStream(myFile);
				in2 = new ObjectInputStream(fileIn);
				users = (ArrayList<User>) in2.readObject();
				in2.close();
				fileIn.close();
			} catch (EOFException e) {
			} 
		}
		synchronized (myFile2) {
			FileInputStream fileIn2;
			ObjectInputStream in3;
			try {
				fileIn2 = new FileInputStream(myFile2);
				in3 = new ObjectInputStream(fileIn2);
				pendingRequest = (CopyOnWriteArrayList<PendingFriend>) in3.readObject();
				in3.close();
				fileIn2.close();
			} catch (EOFException e) {
			} 
		}
	}

}
