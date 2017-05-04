import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PendingFriend implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String usr1;
	private String usr2;
	private long start;

	public PendingFriend(String usr1,String usr2) {
		this.usr1=usr1;
		this.usr2=usr2;
		this.start=System.currentTimeMillis();

	}
	public String getUsr1(){
		return usr1;
	}
	public String getUsr2(){
		return usr2;
	}
	public long getTime(){
		return this.start;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (obj == null)
			return false;
		else {
			PendingFriend u1 = (PendingFriend) obj;
			Boolean a=false,b=false;
			if(u1.getUsr1().equals(usr1)||u1.getUsr1().equals(usr2))
				a=true;
			if(u1.getUsr2().equals(usr1)||u1.getUsr2().equals(usr2))
				b=true;
			return (a&b);
		}
	}

}
