package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;


public class HandleStoreTask extends Task implements Runnable {
	byte[] fileID;
	int chunkNo;

	public HandleStoreTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
