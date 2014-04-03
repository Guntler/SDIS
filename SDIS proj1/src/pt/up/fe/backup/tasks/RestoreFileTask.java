package pt.up.fe.backup.tasks;

import pt.up.fe.backup.BackupFile;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.TaskManager;

public class RestoreFileTask extends Task{
	String filename;
	
	public RestoreFileTask(String filename) {
		super();
		this.filename = filename;
	}

	@Override
	public void run() {
		BackupFile file = DistributedBackupSystem.fManager.getFileByName(filename);
		int curChunk=0;
		if(file != null) {
			System.out.println("File exists");
			for(curChunk=0;curChunk<file.getNumChunks();curChunk++) {
				try {
					DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.RESTORECHUNK, file.getFileID(), curChunk).get();
				} catch (Exception e) {e.printStackTrace();}
			}
		}
	}
}