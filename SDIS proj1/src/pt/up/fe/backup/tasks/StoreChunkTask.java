package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class StoreChunkTask extends Task {
	private BackupChunk chunk;

	public StoreChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		
		FileManager.returnTypes result = DistributedBackupSystem.fManager.saveChunk(chunk);
		
		if(result != FileManager.returnTypes.FAILURE) {
			int waitTime = (int)(Math.random() * 400);
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e1) {e1.printStackTrace();}
		
			try {
				DistributedBackupSystem.cManager.sendPacket(new Packet("STORED", "1.0", chunk.getFileID(), chunk.getChunkNo(), 0, null, null), CommunicationManager.Channels.MC);
			} catch (IOException e) {e.printStackTrace();}
		}
	}

}