package pt.up.fe.backup.tasks;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.FileManager;

public class StoreChunkTask extends Task {
	BackupChunk chunk;

	public StoreChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
