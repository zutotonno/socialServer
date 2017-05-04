import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GestSocialImpl extends RemoteObject implements GestSocial {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GestSocialImpl() {
	}

	@Override
	public boolean followUser(String usr1, String usr2, String token) throws RemoteException {
		int tok = Integer.parseInt(token);
		boolean trovato = false;
		for (User usr : Main_Server.users) {
			if (usr.getUsername().equals(usr1)) {
				if (usr.checkToken() == tok) {
					for (User u : Main_Server.users) {
						if (u.getUsername().equals(usr2)) {
							if (u.addFollower(usr1))
								trovato = true;
						}
					}
				}
			}
		}
		return trovato;
	}

	@Override
	public int addCallBack(String user, ClientSocial c) throws RemoteException {
		int cod = 0;
		for (User u : Main_Server.users) {
			if (u.getUsername().equals(user)) {
				u.setCallBack(c);
				cod = 200;
			}
		}
		if (cod != 200)
			cod = 301;
		return cod;
	}

}
