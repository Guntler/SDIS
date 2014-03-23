package pt.up.fe.backup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.ReceiveChunkTask;
import pt.up.fe.backup.tasks.StoreChunkTask;

public class TaskManager {
	public enum TaskTypes {BACKUPCHUNK, STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATESTORED, HANDLE_REMOVE, DELETECHUNK, RESTORECHUNK};
	
	//private ArrayList<Thread> tasks;
	private FileManager fManager;
	ExecutorService executor = null;
	
	public TaskManager(FileManager fManager) {
		executor = Executors.newFixedThreadPool(5);
		this.fManager = fManager;
	}

	public void setfManager(FileManager fManager) {
		this.fManager = fManager;
	}
	
	public Future<?> executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			return executor.submit(new BackUpChunkTask(fManager, chunk));
		case STORECHUNK:
			return executor.submit(new StoreChunkTask(fManager, chunk));
		case RECEIVECHUNK:
			return executor.submit(new ReceiveChunkTask(fManager, chunk));
		default:
			return null;
		}
	}
}
