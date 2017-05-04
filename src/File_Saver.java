import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class File_Saver implements Runnable {
	File myFile, myFile2;
	ArrayList<User> users;
	CopyOnWriteArrayList<PendingFriend> pendingRequest;
	long pending_duration;

	public File_Saver(File myFile, ArrayList<User> users, File myFile2, CopyOnWriteArrayList<PendingFriend> pendingRequest2,
			long pending_duration) {
		this.myFile = myFile;
		this.users = users;
		this.myFile2 = myFile2;
		this.pendingRequest = pendingRequest2;
		this.pending_duration = pending_duration;
	}

	// TOSAVE PENDINGREQUEST
	@Override
	public void run() {
		FileOutputStream fileOut, fileOut2;
		while (true) {
			try {
				Thread.sleep(10200);
				fileOut = new FileOutputStream(myFile);
				ObjectOutputStream outFile = new ObjectOutputStream(fileOut);
				outFile.writeObject(users);
				outFile.close();
				fileOut.close();
				fileOut2 = new FileOutputStream(myFile2);
				ObjectOutputStream outFile2 = new ObjectOutputStream(fileOut2);
				synchronized (pendingRequest) {
					for (PendingFriend p : pendingRequest) {
						if (System.currentTimeMillis() - p.getTime() < pending_duration) {
						} else {
							pendingRequest.remove(p);
						}
					}
				}
				outFile2.writeObject(pendingRequest);
				outFile2.close();
				fileOut2.close();
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
