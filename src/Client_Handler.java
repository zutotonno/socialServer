import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client_Handler implements Runnable {
	Socket client;
	ArrayList<User> users;
	String ok = "200\r";
	String wr_pass = "300\r";
	String wr_us = "301\r";
	String wr_token = "302\r";
	String usr_offline = "303\r";
	String remote_error = "304\r";
	String saved_4_later = "201\r";
	CopyOnWriteArrayList<PendingFriend> pendingRequest;
	ArrayList<String> online;

	public Client_Handler(Socket client, ArrayList<User> users2, CopyOnWriteArrayList<PendingFriend> pendingRequest2,
			ArrayList<String> online2) {
		this.client = client;
		this.users = users2;
		this.pendingRequest = pendingRequest2;
		this.online = online2;
	}

	@Override
	public void run() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
			System.out.println("New client acc:" + client.getInetAddress());
			String select = in.readLine();
			System.out.println(select);

			if (select.equals("reg")) {
				regUser(in, out);
			} else if (select.equals("login")) {
				logUser(in, out);
			} else if (select.equals("open")) {
				String action = in.readLine();
				System.out.println(action);
				// <Action>
				if (action.equals("0"))
					send_Friendship_Request(in, out);
				if (action.equals("1"))
					accept_Friendship_Request(in, out);
				if (action.equals("3"))
					get_FriendList(in, out);
				if (action.equals("4"))
					find_User(in, out);
				if (action.equals("5"))
					publish_Content(in, out);
				if (action.equals("8"))
					outUser(in, out);
			}
		} catch (IOException e) {
			 e.printStackTrace();
		}
	}

	private void publish_Content(BufferedReader in, BufferedWriter out) {
		try {
			String usr = in.readLine();
			String tkn = in.readLine();
			String content = in.readLine();
			int tok = Integer.parseInt(tkn);
			boolean trovato = false;
			synchronized (users) {
				for (User u : users) {
					if (u.getUsername().equals(usr)) {
						if (u.checkToken() == tok) {
							u.setOnLine(true);
							trovato = true;
							try {
								int count = 0;
								for (String s : u.getFollowers()) {
									for (User usr2 : users) {
										if (usr2.getUsername().equals(s)) {
											if (online.contains(usr2.getUsername()) || usr2.isOnLine()) {
												System.out.println("followers:" + usr2.getUsername());
												try {
													usr2.getCallBack().message(content);
													count++;
												} catch (RuntimeException e1) {
													//e1.printStackTrace();
													System.out.println("ADDING MESSAGE");
													usr2.addOffline_messages(content);
												}
											} else {
												System.out.println("ADDING MESSAGE");
												usr2.addOffline_messages(content);
											}
										}
									}
								}
								if (count > 0) {
									out.write(ok);
									out.flush();
								} else {
									System.out.println("none online");
									out.write(saved_4_later);
									out.flush();
								}
							} catch (RemoteException e) {
								out.write(remote_error);
								out.flush();
								e.printStackTrace();
							}
						} else {
							out.write(wr_token);
							out.flush();
						}
					}
				}
				if (!trovato) {
					out.write(wr_us);
					out.flush();
				}

			}
		} catch (IOException e) {
			//e.printStackTrace();
		}

	}

	private void get_FriendList(BufferedReader in, BufferedWriter out) throws IOException {
		String usr = in.readLine();
		String tkn = in.readLine();
		int tok = Integer.parseInt(tkn);
		Iterator<User> listUser = users.iterator();
		boolean trovato = false;
		synchronized (users) {
			while (listUser.hasNext() && !trovato) {
				User u1 = listUser.next();
				if (u1.getUsername().equals(usr)) {
					trovato = true;
					if (u1.checkToken() == tok) {
						u1.setOnLine(true);
						trovato = true;
						out.write(ok);
						out.flush();
						int n = u1.getFriedsList().size();
						out.write(n + "\r");
						for (String s : u1.getFriedsList()) {
							out.write(s + "\r");
							out.flush();
							boolean status;
							synchronized (online) {
								status=online.contains(s);
							}
							if (status) {
								out.write("ONLINE\r");
								out.flush();
							} else {
								out.write("OFFLINE\r");
								out.flush();
							}
						}
					} else {
						out.write(wr_token);
						out.flush();
						trovato = true;
					}
				}
			}
			if (!trovato) {
				out.write(wr_us);
				out.flush();
			}
		}
	}

	private void find_User(BufferedReader in, BufferedWriter out) throws IOException {
		String usr1 = in.readLine();
		String tkn = in.readLine();
		String usr_to_find = in.readLine();
		int tok = Integer.parseInt(tkn);
		Iterator<User> listUser1 = users.iterator();
		boolean trovato = false;
		synchronized (users) {
			while (listUser1.hasNext() && !trovato) {
				User u1 = listUser1.next();
				if (u1.getUsername().equals(usr1)) {
					trovato = true;
					if (u1.checkToken() == tok) {
						u1.setOnLine(true);
						trovato = true;
						out.write(ok);
						out.flush();
						int dim = 0;
						//<find_String_in_users>
						ArrayList<String> aux = new ArrayList<String>();
						for (User u : users) {
							String us = u.getUsername();
							if (us.length() >= usr_to_find.length()) {
								for (int i = 0; i < us.length(); i++) {
									int count = 0;
									if (us.charAt(i) == usr_to_find.charAt(0))
										if (us.length() - i >= usr_to_find.length()) {
											for (int j = 0; j < usr_to_find.length(); j++) {
												if (us.charAt(i + j) == usr_to_find.charAt(j))
													count++;
											}
											if (count == usr_to_find.length()) {
												dim++;
												i = us.length();
												aux.add(us);
											}
										}
								}
							}
						}
						//</find_String_in_users>
						String u_size = dim + "\r";
						out.write(u_size);
						out.flush();
						for (String u : aux) {
							out.write(u + "\r");
							out.flush();
						}
					} else {
						out.write(wr_token);
						out.flush();
						trovato = true;
					}
				}
			}
			if (!trovato) {
				out.write(wr_us);
				out.flush();
			}
		}

	}

	private void accept_Friendship_Request(BufferedReader in, BufferedWriter out) throws IOException {
		String usr1 = in.readLine();
		String tkn = in.readLine();
		String usr2 = in.readLine();
		String decision = in.readLine();
		int tok = Integer.parseInt(tkn);
		boolean trovato = false;
		Iterator<User> listUser1 = users.iterator();
		Iterator<User> listUser2 = users.iterator();
		synchronized (users) {
			while (listUser1.hasNext() && !trovato) {
				User u1 = listUser1.next();
				if (u1.getUsername().equals(usr1)) {
					trovato = true;
					if (u1.checkToken() == tok) {
						u1.setOnLine(true);
						boolean trovato2 = !trovato;
						while (listUser2.hasNext() && !trovato2) {
							User u2 = listUser2.next();

							if (u2.getUsername().equals(usr2)) {
								trovato2 = true;
								boolean trovato3 = !trovato2;
								Iterator<PendingFriend> pendingList = pendingRequest.iterator();
								synchronized (pendingRequest) {
									while (pendingList.hasNext() && !trovato3) {
										PendingFriend pf = pendingList.next();
										if (pf.getUsr2().equals(usr1) && pf.getUsr1().equals(usr2)) {
											trovato3 = true;
											if (decision.equals("accept")) {
												u1.addFriend(usr2);
												u2.addFriend(usr1);
												out.write(ok);
												out.flush();

												pendingRequest.remove(pf);
											}
										}
										if (trovato3 && decision.equals("reject")) {
											pendingRequest.remove(pf);
											out.write(ok);
											out.flush();
										}

									}
									if (!trovato3) {
										out.write(wr_us);
										out.flush();
									}
								}
							}
						}
						if (!trovato2) {
							out.write(wr_us);
							out.flush();
						}
					} else {
						trovato = true;
						out.write(wr_token);
						out.flush();
					}
				}
			}
		}
		if (!trovato) {
			out.write(wr_us);
			out.flush();
		}

	}

	private void send_Friendship_Request(BufferedReader in, BufferedWriter out) throws IOException {
		String usr1 = in.readLine();
		String tkn = in.readLine();
		String usr2 = in.readLine();
		int tok = Integer.parseInt(tkn);
		boolean trovato = false;
		synchronized (users) {
			for (User usr : users) {
				if (usr.getUsername().equals(usr1)) {
					if (usr.checkToken() == tok) {
						usr.setOnLine(true);
						if (!usr.getFriedsList().contains(usr2)) {
							for (User u : users) {
								if (u.getUsername().equals(usr2)) {
									int cod = sendRequest(u.getUsername(), u.getHost(), u.getPORT(), usr1);
									if (cod == 0) {
										synchronized (pendingRequest) {
											if (!pendingRequest.contains(new PendingFriend(usr1, usr2)))
												pendingRequest.add(new PendingFriend(usr1, usr2));
										}
										System.out.println("REQUEST SENT");
										u.setOnLine(true);
										out.write(ok);
										out.flush();
										trovato = true;
									} else {
										out.write(usr_offline);
										out.flush();
										trovato = true;
									}
								}
							}
						} else {
							out.write(wr_us);
							out.flush();
						}
					} else {
						out.write(wr_token);
						out.flush();
					}
				}
			}
			if (!trovato) {
				out.write(wr_us);
				out.flush();
			}
		}
	}

	private int sendRequest(String username, String host, int port, String usr1) {
		System.out.println("Sending to: " + username + " " + host + port + " FROM:" + usr1);
		try {
			if (host != null) {
				Socket c = new Socket();
				c.connect(new InetSocketAddress(host, port));
				BufferedWriter b = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
				b.write(usr1 + "\r");
				b.flush();
				b.close();
				c.close();
				return 0;
			} else {
				return -1;
			}
		} catch (SocketException e) {
			return -1;
		} catch (IOException e) {
			return -1;
		}
	}

	private void outUser(BufferedReader in, BufferedWriter out) throws IOException {
		String user = in.readLine();
		String tkn = in.readLine();
		int tok = Integer.parseInt(tkn);
		boolean trovato = false;
		synchronized (users) {
			for (User usr : users) {
				if (usr.getUsername().equals(user)) {
					System.out.println(usr.getUsername() + " " + usr.checkToken());
					if (usr.checkToken() == tok) {
						usr.resetToken();
						usr.setOnLine(false);
						synchronized (online) {
							online.remove(usr.getUsername());
						}
						usr.setCallBack(null);
						out.write(ok);
						out.flush();
						trovato = true;
						break;
					} else {
						out.write(wr_token);
						out.flush();
					}
				}
			}
			if (!trovato) {
				out.write(wr_us);
				out.flush();
			}
		}
	}

	private void regUser(BufferedReader in, BufferedWriter out) throws IOException {
		String name = in.readLine();
		System.out.println(name);
		String pass = in.readLine();
		System.out.println(pass);
		User u = new User(name, pass);
		synchronized (users) {
			if (!users.contains(u)) {
				users.add(u);
				out.write(ok);
				out.flush();
				System.out.println(name + " " + pass);
			} else {
				out.write(wr_us);
				out.flush();
			}
		}
	}

	private void logUser(BufferedReader in, BufferedWriter out) throws IOException {
		String name = in.readLine();
		System.out.println(name);
		String pass = in.readLine();
		System.out.println(pass);
		User actual = null;
		boolean trovato = false;
		synchronized (users) {
			for (User usr : users) {
				if (usr.getUsername().equals(name)) {
					if (usr.getPassword().equals(pass)) {
						out.write(ok);
						out.flush();
						String host = in.readLine();
						System.out.println(host);
						String port = in.readLine();
						int p = Integer.parseInt(port);
						System.out.println(p);
						usr.logIn();
						actual = usr;
						synchronized (online) {
							online.add(name);
						}
						out.write(usr.checkToken() + "\r");
						out.flush();
						System.out.println(usr.checkToken());
						usr.setHost(host);
						usr.setPORT(p);
						trovato = true;

					} else {
						out.write(wr_pass);
						out.flush();
						trovato = true;
					}
				}
			}
			if (!trovato) {
				out.write(wr_us);
				out.flush();
			}
		}
		if (actual != null && actual.getCallBack() != null) {
			for (String m : actual.getOffline_messages()) {
				System.out.println("Sending " + m);
				try {
					System.out.println("TO:" + actual.getCallBack());
					actual.getCallBack().message(m);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			actual.getOffline_messages().clear();
		}
	}

}
