package pt.up.fe.backup.tasks;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;

/**
 * Takes place when the User issues a REMOVED packet. Deletes the chunk from memory and from the file system,
 * then sends a REMOVED message to the channels.
 * @author pbpdi_000
 *
 */
public class RemoveTask extends Task {
	private byte[] fileID;
	private int chunkNo;

	public RemoveTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.releaseSpace(); //TODO removes all chunks with rep degree above necessary and sends removed packet for each
	}
}
