import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.time.LocalTime;
import java.util.ArrayList;

public class Receiver implements Runnable {
	DatagramChannel serverUDP;
	ArrayList<String> online;
	long time;

	public Receiver(DatagramChannel serverA, ArrayList<String> onLine) {
		this.serverUDP = serverA;
		this.online = onLine;
	}

	@Override
	public void run() {
		try {
			while (true) {
				ByteBuffer dst = ByteBuffer.allocate(64);
				serverUDP.receive(dst);
				dst.flip();
				dst.getInt();
				char c;
				StringBuilder sb = new StringBuilder();
				while ((c = dst.getChar()) != '#') {
					sb.append(c);
				}
				String ris = sb.toString();
				synchronized (online) {
					online.add(ris);
				}
			}
		} catch (IOException e) {

		}
	}

}
