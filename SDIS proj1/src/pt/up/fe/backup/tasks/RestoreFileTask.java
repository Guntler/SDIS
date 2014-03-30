package pt.up.fe.backup.tasks;

import java.util.concurrent.ExecutionException;

import pt.up.fe.backup.BackupFile;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.TaskManager;

public class RestoreFileTask extends Task{
	String filename;
	
	public RestoreFileTask(FileManager fManager, String filename) {
		super(fManager);
		this.filename = filename;
	}

	@Override
	public void run() {
		BackupFile file = DistributedBackupSystem.fManager.getFileByName(filename);
		System.out.println(file.getFilename());
		System.out.println("Found " + Integer.toString(file.getNumChunks()) + " chunks.");
		for(int i=0;i<file.getNumChunks(); i++) {
			try {
				DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.RESTORECHUNK, file.getFileID(), i).get();
			} catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
		}
	}
}