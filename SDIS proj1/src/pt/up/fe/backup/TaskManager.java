package pt.up.fe.backup;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.ReceiveChunkTask;
import pt.up.fe.backup.tasks.StoreChunkTask;
import pt.up.fe.backup.tasks.Task;

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
	
	public void executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			executor.execute(new BackUpChunkTask(fManager, chunk));
			break;
		case STORECHUNK:
			executor.execute(new StoreChunkTask(fManager, chunk));
			break;
		/*case RECEIVECHUNK:
			executor.execute(new ReceiveChunkTask(fManager, chunk));
			break;*/
		}
		
	}
}
