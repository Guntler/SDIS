package pt.up.fe.backup.tasks;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;

public class SetAllocatedMemoryTask extends Task {

	private long newMemory;
	
	public SetAllocatedMemoryTask(FileManager fManager, long repDeg) {
		super(fManager);
		this.newMemory = repDeg;
	}
	
	@Override
	public void run() {
		DistributedBackupSystem.fManager.setMaxSize(newMemory);
		DistributedBackupSystem.fManager.releaseSpace();
	}

}
