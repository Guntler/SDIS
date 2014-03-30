package pt.up.fe.backup.tasks;

import java.net.InetAddress;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;

/**
 * Takes place when a packet REMOVED is received. Deletes the chunk from memory and from the file system.
 * @author pbpdi_000
 *
 */
public class HandleRemoveTask extends Task {
	private byte[] fileID;
	private int chunkNo;
	private InetAddress addr;

	public HandleRemoveTask(FileManager fManager, byte[] fileID, int chunkNo, InetAddress addr) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.updateRepDegree(fileID, chunkNo, addr, false);
	}
}
