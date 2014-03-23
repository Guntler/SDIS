package pt.up.fe.backup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.DeleteTask;
import pt.up.fe.backup.tasks.HandleRemoveTask;
import pt.up.fe.backup.tasks.ReceiveChunkTask;
import pt.up.fe.backup.tasks.RestoreChunkTask;
import pt.up.fe.backup.tasks.SendChunkTask;
import pt.up.fe.backup.tasks.StoreChunkTask;
<<<<<<< HEAD
import pt.up.fe.backup.tasks.Task;
import pt.up.fe.backup.tasks.UpdateStoredTask;
=======
>>>>>>> 99a5f5f9ff5e4df8835b00830935071dc0ee978c

public class TaskManager {
	public enum TaskTypes {BACKUPCHUNK, STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATESTORED, HANDLE_REMOVE, DELETE, RESTORECHUNK};
	
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
<<<<<<< HEAD
	}
	
	public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo) {
		switch(type) {
		case SENDCHUNK:
			return executor.submit(new SendChunkTask(fManager, fileID, chunkNo));
		case UPDATESTORED:
			return executor.submit(new UpdateStoredTask(fManager, fileID, chunkNo));
		case HANDLE_REMOVE:
			return executor.submit(new HandleRemoveTask(fManager, fileID, chunkNo));
		case DELETE:
			return executor.submit(new DeleteTask(fManager, fileID));
		case RESTORECHUNK:
			return executor.submit(new RestoreChunkTask(fManager, fileID, chunkNo));
		default:
			return null;
		}
=======
>>>>>>> 99a5f5f9ff5e4df8835b00830935071dc0ee978c
	}
}
