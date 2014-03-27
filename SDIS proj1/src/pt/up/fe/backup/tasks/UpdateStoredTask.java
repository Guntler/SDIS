package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

public class UpdateStoredTask extends Task {
	byte[] fileID;
	int chunkNo;

	public UpdateStoredTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
