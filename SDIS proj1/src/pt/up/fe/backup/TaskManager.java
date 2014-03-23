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
import pt.up.fe.backup.tasks.UpdateStoredTask;

public class TaskManager {
	public enum TaskTypes {BACKUPCHUNK, STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATESTORED, HANDLE_REMOVE, DELETE, RESTORECHUNK};
	
	private DistributedBackupSystem dbs;
	ExecutorService executor = null;
	
	public TaskManager(DistributedBackupSystem dbs) {
		executor = Executors.newFixedThreadPool(5);
		this.dbs = dbs;
	}
	
	public Future<?> executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			return executor.submit(new BackUpChunkTask(dbs.getFManager(), dbs.getCManager(), chunk));
		case STORECHUNK:
			return executor.submit(new StoreChunkTask(dbs.getFManager(), dbs.getCManager(), chunk));
		case RECEIVECHUNK:
			return executor.submit(new ReceiveChunkTask(dbs.getFManager(), dbs.getCManager(), chunk));
		default:
			return null;
		}
	}
	
	public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo) {
		switch(type) {
		case SENDCHUNK:
			return executor.submit(new SendChunkTask(dbs.getFManager(), dbs.getCManager(), fileID, chunkNo));
		case UPDATESTORED:
			return executor.submit(new UpdateStoredTask(dbs.getFManager(), dbs.getCManager(), fileID, chunkNo));
		case HANDLE_REMOVE:
			return executor.submit(new HandleRemoveTask(dbs.getFManager(), dbs.getCManager(), fileID, chunkNo));
		case DELETE:
			return executor.submit(new DeleteTask(dbs.getFManager(), dbs.getCManager(), fileID));
		case RESTORECHUNK:
			return executor.submit(new RestoreChunkTask(dbs.getFManager(), dbs.getCManager(), fileID, chunkNo));
		default:
			return null;
		}
	}
	
	public Future<?> executeTask(Packet packet) {
		if(packet.packetType.equals("PUTCHUNK")) {
			return executor.submit(new StoreChunkTask(dbs.getFManager(), dbs.getCManager(), packet.getChunk()));
		}
		else if (packet.packetType.equals("GETCHUNK")) {
			return executor.submit(new SendChunkTask(dbs.getFManager(), dbs.getCManager(), packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("DELETE")) {
			return executor.submit(new DeleteTask(dbs.getFManager(), dbs.getCManager(), packet.getFileID()));
		}
		else if (packet.packetType.equals("REMOVED")) {
			return executor.submit(new HandleRemoveTask(dbs.getFManager(), dbs.getCManager(), packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("STORED")) {
			return executor.submit(new UpdateStoredTask(dbs.getFManager(), dbs.getCManager(), packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("CHUNK")) {
			return executor.submit(new ReceiveChunkTask(dbs.getFManager(), dbs.getCManager(), packet.getChunk()));
		}
		return null;
	}
}
