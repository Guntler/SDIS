package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

public class DeleteTask extends Task {
	byte[] fileID;

	public DeleteTask(FileManager fManager, byte[] fileID) {
		super(fManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
