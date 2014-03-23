package pt.up.fe.backup.tasks;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;

public class BackUpChunkTask extends Task {
	BackupChunk chunk;
	
	public BackUpChunkTask(FileManager fManager, CommunicationManager cManager, BackupChunk chunk) {
		super(fManager, cManager);
		this.chunk = chunk;
	}
	
	@Override
	public void run() {
		
	}
}
