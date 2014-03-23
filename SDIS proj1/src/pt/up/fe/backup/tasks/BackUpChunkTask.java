package pt.up.fe.backup.tasks;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.FileManager;

public class BackUpChunkTask extends Task {
	FileManager fManager;
	BackupChunk chunk;
	
	public BackUpChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}
	
	@Override
	public void run() {
		
	}
}
