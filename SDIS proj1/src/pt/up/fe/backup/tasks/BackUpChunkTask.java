package pt.up.fe.backup.tasks;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.FileManager;

public class PutChunk extends Task {
	FileManager fManager;
	
	public PutChunk(FileManager fManager) {
		super(fManager);
	}
	
	@Override
	public void run() {
		
	}
}
