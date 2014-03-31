package pt.up.fe.backup;

import java.util.concurrent.Future;
import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.BackupFileTask;
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
	public enum TaskTypes {BACKUPFILE, BACKUPCHUNK, STORECHUNK, SENDCHUNK, HANDLE_REMOVE, HANDLE_STORE, DELETEFILE, DELETE, HANDLE_DELETE, REMOVE, RESTORECHUNK, RESTOREFILE, SETMEMORY};
	
	private DistributedBackupSystem dbs;
	TaskExecutor executor = null;
	
	public TaskManager(DistributedBackupSystem dbs) {
		executor = new TaskExecutor(10);
		this.dbs = dbs;
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			return executor.submit(new BackUpChunkTask(dbs.getFManager(), chunk));
		case STORECHUNK:
			return executor.submit(new StoreChunkTask(dbs.getFManager(), chunk));
		case REMOVE:
			return executor.submit(new RemoveTask(dbs.getFManager()));
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo) {
		switch(type) {
		case DELETE:
			return executor.submit(new DeleteTask(dbs.getFManager(), fileID));
		case RESTORECHUNK:
			return executor.submit(new RestoreChunkTask(dbs.getFManager(), fileID, chunkNo));
		case REMOVE:
			return executor.submit(new RemoveTask(dbs.getFManager()));
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(Packet packet) {
		if(packet.packetType.equals("PUTCHUNK")) {
			executor.messageActiveTasks(packet);
			return executor.submit(new StoreChunkTask(dbs.getFManager(), packet.getChunk()));
		}
		else if (packet.packetType.equals("GETCHUNK")) {
			return executor.submit(new SendChunkTask(dbs.getFManager(), packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("DELETE")) {
			return executor.submit(new HandleDeleteTask(dbs.getFManager(), packet.getFileID()));
		}
		else if (packet.packetType.equals("REMOVED")) {
			return executor.submit(new HandleRemoveTask(dbs.getFManager(), packet.getFileID(), packet.getChunkNo(), packet.getSenderAddress()));
		}
		else if (packet.packetType.equals("STORED")) {
			executor.messageActiveTasks(packet);
			return executor.submit(new HandleStoreTask(dbs.getFManager(), packet.getFileID(), packet.getChunkNo(), packet.getSenderAddress()));
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
			return executor.submit(new BackupFileTask(null, name, (int)repDeg));
		}
		else if(type == TaskTypes.RESTOREFILE) {
			return executor.submit(new RestoreFileTask(null, name));
		}
		else if(type == TaskTypes.DELETEFILE) {
			return executor.submit(new DeleteFileTask(null, name));
		}
		else if(type == TaskTypes.SETMEMORY) {
			return executor.submit(new SetAllocatedMemoryTask(null, repDeg));
		}
		else return null;
			
		
	}
}
