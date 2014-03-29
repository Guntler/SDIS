package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

/**
 * Takes place when a packet REMOVED is received. Deletes the chunk from memory and from the file system.
 * @author pbpdi_000
 *
 */
public class HandleRemoveTask extends Task {
	byte[] fileID;
	int chunkNo;

	public HandleRemoveTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		fManager.deleteChunk(fileID,chunkNo);
		//write info to log
	}
}
