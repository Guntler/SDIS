package pt.up.fe.backup.tasks;

import java.io.File;
import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class StoreChunkTask extends Task {
	BackupChunk chunk;

	public StoreChunkTask(FileManager fManager, CommunicationManager cManager, BackupChunk chunk) {
		super(fManager, cManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		
		boolean result = fManager.saveChunk(chunk);
		
		if(result) {
			try {
				cManager.sendPacket(new Packet("STORED", "1.0.0", chunk.getFileID(), chunk.getChunkNo(), chunk.getWantedReplicationDegree(), null), CommunicationManager.Channels.MC);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
