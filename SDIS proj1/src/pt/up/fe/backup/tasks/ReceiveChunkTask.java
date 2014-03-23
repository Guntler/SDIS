package pt.up.fe.backup.tasks;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;

public class ReceiveChunkTask extends Task {
	BackupChunk chunk;

	public ReceiveChunkTask(FileManager fManager, CommunicationManager cManager, BackupChunk chunk) {
		super(fManager, cManager);
		this.chunk = chunk;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
