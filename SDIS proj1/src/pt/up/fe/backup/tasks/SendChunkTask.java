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
	private byte[] fileID;
	private int chunkNo;

	public SendChunkTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		boolean done = false;

		BackupChunk chunk = fManager.getChunk(fileID, chunkNo);
		
		try {
			int waitTime = (int)(Math.random() * 400);
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			
			for(Packet p : messages)
				if(p.getPacketType().equals("CHUNK") && p.getFileID().equals(this.fileID) && p.getChunkNo() == this.chunkNo)
					done = true;
			
			if(!done)
				DistributedBackupSystem.cManager.sendPacket(new Packet("CHUNK", "1.0", fileID, chunkNo, 0, chunk.getData(), null),
															CommunicationManager.Channels.MDR);
		} catch (IOException e) {e.printStackTrace();}
	}

}
