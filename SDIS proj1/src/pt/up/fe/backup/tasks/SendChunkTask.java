package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

/**
 * Takes place when a GETCHUNK packet is received. Gets the chunk that matches the info provided, 
 * then sends a packet CHUNK to the network.
 * @author pbpdi_000
 *
 */
public class SendChunkTask extends Task {
	byte[] fileID;
	int chunkNo;

	public SendChunkTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		boolean done = false;
		int waitTime = 500;

		BackupChunk chunk = fManager.getChunk(fileID, chunkNo);
		
		try {
			try {
				Thread.sleep(waitTime);
				
				for(Packet p : messages) {
					if(p.getPacketType().equals("CHUNK") && p.getFileID().equals(this.fileID) && p.getChunkNo() == this.chunkNo) {
						done = true;
						//write info to log ????
					}
				}
				
				if(!done) {
					Packet pack = new Packet("CHUNK", "1.0", fileID, chunkNo, 0, chunk.getData());
					DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MDR);
					//write info to log
				}
			} catch (InterruptedException e) {e.printStackTrace();}
		} catch (IOException e) {e.printStackTrace();}
	}

}
