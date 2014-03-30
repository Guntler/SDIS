package pt.up.fe.backup.tasks;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;

/**
 * Deletes the file from the local system. 
 * @author pbpdi_000
 *
 */
public class HandleDeleteTask extends Task {
	private byte[] fileID;

	public HandleDeleteTask(FileManager fManager, byte[] fileID) {
		super(fManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.deleteAllChunks(fileID);
	}

}
