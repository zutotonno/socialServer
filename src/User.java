import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
	private String password;
	private String host;
	private int PORT;
	private int token;
	private long start, end;
	private Lock lock,lock_mess;
	private boolean onLine;
	private ArrayList<String> friends;
	private ClientSocial callBack;
	private ArrayList<String> followers;
	private ArrayList<String> offline_messages;
	public User(String name, String pass) {
		userName = name;
		password = pass;
		token = -1;
		this.lock = new ReentrantLock();
		this.lock_mess= new ReentrantLock();
		this.friends = new ArrayList<String>();
		this.onLine=false;
		this.followers = new ArrayList<String>();
		this.offline_messages= new ArrayList<String>();
	}
	
	public ArrayList<String> getFollowers(){
		return this.followers;
	}
	
	public synchronized boolean addFollower(String f){
		if(!followers.contains(f)){
			lock.lock();
			followers.add(f);
			lock.unlock();
			return true;
		}
		return false;
	}
	
	public synchronized void addFriend(String f){
		if(!friends.contains(f))
			friends.add(f);
	}
	
	public synchronized ArrayList<String> getFriedsList(){
		return this.friends;
	}
	public synchronized boolean isFriend(String s){
		if(friends.contains(s))
			return true;
		return false;
	}
	public String getUsername() {
		return this.userName;
	}

	public String getPassword() {
		return this.password;
	}
	public int logIn() {
		lock.lock();
		start = System.currentTimeMillis();
		token = (ThreadLocalRandom.current().nextInt(1,500));
		lock.unlock();
		return token;
	}
	public int checkToken(){
		end = System.currentTimeMillis();
		if (end - start < 86400000){ // 24H
			return this.token;
		}
		else{
			//System.err.println("ERROOOOOR");
			return -1;
		}
	}
	
	public void resetToken(){
		lock.lock();
		this.token=-1;
		lock.unlock();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else {
			User u = (User) obj;
			return this.userName.equals(u.getUsername());
		}
	}

	public boolean isOnLine() {
		return onLine;
	}

	public void setOnLine(boolean onLine) {
		lock.lock();
		this.onLine = onLine;
		lock.unlock();
	}

	public int getPORT() {
		return PORT;
	}

	public void setPORT(int pORT) {
		PORT = pORT;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public ClientSocial getCallBack() {
		return callBack;
	}

	public synchronized void setCallBack(ClientSocial callBack) {
		lock.lock();
		this.callBack = callBack;
		lock.unlock();
	}

	public ArrayList<String> getOffline_messages() {
		ArrayList<String> s;
		lock_mess.lock();
		s=this.offline_messages;
		lock_mess.unlock();
		return s;
	}

	public void addOffline_messages(String message) {
		lock_mess.lock();
		this.offline_messages.add(message);
		lock_mess.unlock();
	}

}
