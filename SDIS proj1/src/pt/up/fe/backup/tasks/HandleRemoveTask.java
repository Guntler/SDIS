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
		DistributedBackupSystem.fManager.assureChunkRepDegree(fileID, chunkNo); //TODO checks if chunk repDegree is too small and if yes waits 0-400 ms, checks messages to see if it received a putchunk (make putchunk go to messages) and if not starts backup subprotocol
	}
}
