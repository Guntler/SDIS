package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

/**
 * Deletes the file from the local system. 
 * @author pbpdi_000
 *
 */
public class HandleDeleteTask extends Task {
	byte[] fileID;

	public HandleDeleteTask(FileManager fManager, byte[] fileID) {
		super(fManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		FileManager.returnTypes result = fManager.deleteAllChunks(fileID);
	}

}
