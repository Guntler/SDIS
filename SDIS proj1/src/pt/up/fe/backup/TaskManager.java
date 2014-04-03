package pt.up.fe.backup;

import java.util.concurrent.Future;
import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.BackupFileTask;
import pt.up.fe.backup.tasks.CheckChunkAvailabilityTask;
import pt.up.fe.backup.tasks.DeleteFileTask;
import pt.up.fe.backup.tasks.DeleteTask;
import pt.up.fe.backup.tasks.HandleDeleteTask;
import pt.up.fe.backup.tasks.HandleRemoveTask;
import pt.up.fe.backup.tasks.HandleStoreTask;
import pt.up.fe.backup.tasks.RemoveTask;
import pt.up.fe.backup.tasks.RestoreChunkTask;
import pt.up.fe.backup.tasks.RestoreFileTask;
import pt.up.fe.backup.tasks.SendChunkTask;
import pt.up.fe.backup.tasks.SetAllocatedMemoryTask;
import pt.up.fe.backup.tasks.StoreChunkTask;

public class TaskManager {
	public enum TaskTypes {BACKUPFILE, BACKUPCHUNK, STORECHUNK, SENDCHUNK, HANDLE_REMOVE, HANDLE_STORE, DELETEFILE, DELETE, HANDLE_DELETE, REMOVE, RESTORECHUNK, RESTOREFILE, SETMEMORY,CHECKCHUNK};

	private TaskExecutor executor = null;
	
	public TaskManager() {
		executor = new TaskExecutor(10);
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			return executor.submit(new BackUpChunkTask(chunk));
		case STORECHUNK:
			return executor.submit(new StoreChunkTask(chunk));
		case REMOVE:
			return executor.submit(new RemoveTask());
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo) {
		switch(type) {
		case DELETE:
			return executor.submit(new DeleteTask(fileID));
		case RESTORECHUNK:
			return executor.submit(new RestoreChunkTask(fileID, chunkNo));
		case REMOVE:
			return executor.submit(new RemoveTask());
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(Packet packet) {
		if(packet.packetType.equals("PUTCHUNK")) {
			executor.messageActiveTasks(packet);
			return executor.submit(new StoreChunkTask(packet.getChunk()));
		}
		else if (packet.packetType.equals("GETCHUNK")) {
			return executor.submit(new SendChunkTask(packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("DELETE")) {
			return executor.submit(new HandleDeleteTask(packet.getFileID()));
		}
		else if (packet.packetType.equals("REMOVED")) {
			return executor.submit(new HandleRemoveTask(packet.getFileID(), packet.getChunkNo(), packet.getSenderAddress()));
		}
		else if (packet.packetType.equals("STORED")) {
			executor.messageActiveTasks(packet);
			return executor.submit(new HandleStoreTask(packet.getFileID(), packet.getChunkNo(), packet.getSenderAddress()));
		}
		else if (packet.packetType.equals("CHECKCHUNK")) {
			executor.messageActiveTasks(packet);
			return executor.submit(new CheckChunkAvailabilityTask());
		}
		else if (packet.packetType.equals("CHUNK")) {
			executor.messageActiveTasks(packet);
		}
		return null;
	}
	
	public void finish() {
		executor.shutdownNow();
	}
	
	public void sendMessageToActiveTasks(Packet p) {
		executor.messageActiveTasks(p);
	}

	synchronized public Future<?> executeTask(TaskTypes type, String name, long repDeg) {
		if(type == TaskTypes.BACKUPFILE) {
			return executor.submit(new BackupFileTask(name, (int)repDeg));
		}
		else if(type == TaskTypes.RESTOREFILE) {
			return executor.submit(new RestoreFileTask(name));
		}
		else if(type == TaskTypes.DELETEFILE) {
			return executor.submit(new DeleteFileTask(name));
		}
		else if(type == TaskTypes.SETMEMORY) {
			return executor.submit(new SetAllocatedMemoryTask(repDeg));
		}
		else if(type == TaskTypes.CHECKCHUNK) {
			return executor.submit(new CheckChunkAvailabilityTask());
		}
		else return null;
			
		
	}
}
