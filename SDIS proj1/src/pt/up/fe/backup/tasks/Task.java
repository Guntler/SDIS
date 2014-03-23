package pt.up.fe.backup.tasks;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;

public abstract class Task implements Runnable {
	protected FileManager fManager;
	protected CommunicationManager cManager;
	
	public Task(FileManager fManager, CommunicationManager cManager) {
		this.fManager = fManager;
	}
	
	public abstract void run();
}
