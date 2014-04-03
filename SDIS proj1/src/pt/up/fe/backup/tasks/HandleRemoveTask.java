package pt.up.fe.backup.tasks;

import java.net.InetAddress;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.Packet;
import pt.up.fe.backup.TaskManager;

/**
 * Takes place when a packet REMOVED is received. Deletes the chunk from memory and from the file system.
 * @author pbpdi_000
 *
 */
public class HandleRemoveTask extends Task {
	private byte[] fileID;
	private int chunkNo;
	private InetAddress addr;

	public HandleRemoveTask(byte[] fileID, int chunkNo, InetAddress addr) {
		super();
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.updateRepDegree(fileID, chunkNo, addr, false);
		if(!DistributedBackupSystem.fManager.chunkCorrectRepDegree(fileID, chunkNo)) {
			int waitTime = (int)(Math.random() * 400);
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			for(Packet p : messages) {
				if(p.getPacketType().equals("PUTCHUNK") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(fileID)) && chunkNo == p.getChunkNo()) {
					return;
				}
			}
			
			DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.BACKUPCHUNK, DistributedBackupSystem.fManager.getChunk(fileID, chunkNo));
		}
	}
}
