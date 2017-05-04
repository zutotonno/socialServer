import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Keep_Alive_Sender implements Runnable {
	ArrayList<User> users;
	public static long time;
	ArrayList<String> onLine;
	public Keep_Alive_Sender(ArrayList<User> users, ArrayList<String> online) {
		this.users = users;
		this.onLine=online;
	}

	@Override
	public void run() {
		try (DatagramChannel serverMulticast = DatagramChannel.open();
				DatagramChannel serverUDP = DatagramChannel.open()) {
			SocketAddress group = new InetSocketAddress(InetAddress.getByName(Main_Server.MC_GROUP), Main_Server.MULTICAST_PORT);
			InetSocketAddress UDPs = new InetSocketAddress(InetAddress.getByName(Main_Server.HOST), Main_Server.UDP);
			serverUDP.socket().bind(UDPs);
			serverMulticast.bind(null);
			serverMulticast.connect(group);
			ExecutorService es = Executors.newSingleThreadExecutor();
			Runnable receiver = new Receiver(serverUDP,onLine);
			es.submit(receiver);
			while (true) {
				String msg = "ciao";
				msg += "#";
				ByteBuffer buff = ByteBuffer.allocate((msg.length() * 2) + 4);
				buff.putInt(msg.length());
				for (int i = 0; i < msg.length(); i++)
					buff.putChar(msg.charAt(i));
				buff.flip();
				serverMulticast.write(buff);
				Thread.sleep(10000);
				synchronized (users) {
					for(User u:users){
						if(onLine.contains(u.getUsername())){
							u.setOnLine(true);
						}
						else{
							u.setOnLine(false);
							u.resetToken();
							u.setCallBack(null);
						}
					}
				}
				synchronized (onLine) {
					System.out.println("ONLINE: "+onLine);
					onLine.clear();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
