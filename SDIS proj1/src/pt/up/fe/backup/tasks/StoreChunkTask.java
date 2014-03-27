package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class StoreChunkTask extends Task {
	BackupChunk chunk;

	public StoreChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		
		boolean result = DistributedBackupSystem.fManager.saveChunk(chunk);
		
		if(result) {
			System.out.println("Result is true");
			try {
				DistributedBackupSystem.cManager.sendPacket(new Packet("STORED", "1.0.0", chunk.getFileID(), chunk.getChunkNo(), chunk.getWantedReplicationDegree(), null), CommunicationManager.Channels.MC);
				//write info to log
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Result is false");
	}

}