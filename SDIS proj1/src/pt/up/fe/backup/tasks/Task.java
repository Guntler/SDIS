package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

public abstract class Task implements Runnable {
	protected FileManager fManager;
	
	public Task(FileManager fManager) {
		this.fManager = fManager;
	}
	
	public abstract void run();
}
