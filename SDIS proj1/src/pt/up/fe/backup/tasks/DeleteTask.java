package pt.up.fe.backup.tasks;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;

public class DeleteTask extends Task {
	byte[] fileID;

	public DeleteTask(FileManager fManager, CommunicationManager cManager, byte[] fileID) {
		super(fManager, cManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
