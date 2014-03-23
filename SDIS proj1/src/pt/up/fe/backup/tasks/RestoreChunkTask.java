package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

public class RestoreChunkTask extends Task {
	byte[] fileID;
	int chunkNo;

	public RestoreChunkTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
