import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class Deserializator {
	static ArrayList<User> users = new ArrayList<>();
	static CopyOnWriteArrayList<PendingFriend> pf = new CopyOnWriteArrayList<>();

	public static void main(String[] args) {
		try {
			FileInputStream fileIn = new FileInputStream("list.ser");
			ObjectInputStream in = new ObjectInputStream(fileIn);
			users = (ArrayList<User>) in.readObject();
			in.close();
			fileIn.close();
			for (User u : users) {
				System.out.println(u.getUsername() + " pwd:" + u.getPassword() + " tkn:" + u.checkToken() + " Online:"
						+ u.isOnLine()+ " port:"+u.getPORT()+"host: "+u.getHost()+" friendList: "+u.getFriedsList());
				System.out.println(u.getCallBack());
				System.out.println("Followers: "+u.getFollowers());
				System.out.println("OFFLINE_mex: "+ u.getOffline_messages());
			}
			FileInputStream fileIn2 = new FileInputStream("pending.ser");
			ObjectInputStream in2 = new ObjectInputStream(fileIn2);
			pf = (CopyOnWriteArrayList<PendingFriend>) in2.readObject();
			in2.close();
			fileIn2.close();
			System.out.println("Pending Request");
			for (PendingFriend u : pf) {
				System.out.println("FROM:"+u.getUsr1()+" TO:"+u.getUsr2());
			}
		}
		catch(FileNotFoundException e ){
			System.out.println("File doesn't exist");
		}
		catch (EOFException i) {
			// i.printStackTrace();
			// System.out.println("FUCK");
		} catch (ClassNotFoundException c) {
			System.out.println("User class not found");
			c.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
