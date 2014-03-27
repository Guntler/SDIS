package pt.up.fe.backup.tasks;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.FileManager;

public class ReceiveChunkTask extends Task {
	BackupChunk chunk;

	public ReceiveChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		boolean result = fManager.saveChunk(chunk);
		
	}

}
