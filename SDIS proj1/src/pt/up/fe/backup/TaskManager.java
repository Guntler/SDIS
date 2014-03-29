package pt.up.fe.backup;

import java.util.concurrent.Future;

import pt.up.fe.backup.tasks.BackUpChunkTask;
import pt.up.fe.backup.tasks.BackupFileTask;
import pt.up.fe.backup.tasks.DeleteTask;
import pt.up.fe.backup.tasks.HandleDeleteTask;
import pt.up.fe.backup.tasks.HandleRemoveTask;
import pt.up.fe.backup.tasks.ReceiveChunkTask;
import pt.up.fe.backup.tasks.RestoreChunkTask;
import pt.up.fe.backup.tasks.SendChunkTask;
import pt.up.fe.backup.tasks.StoreChunkTask;
import pt.up.fe.backup.tasks.UpdateStoredTask;

public class TaskManager {
	public enum TaskTypes {BACKUPFILE, BACKUPCHUNK, STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATESTORED, HANDLE_REMOVE, DELETEFILE, DELETE, HANDLE_DELETE, REMOVE, RESTORECHUNK};
	
	private DistributedBackupSystem dbs;
	TaskExecutor executor = null;
	
	public TaskManager(DistributedBackupSystem dbs) {
		executor = new TaskExecutor(5);
		this.dbs = dbs;
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, BackupChunk chunk) {
		switch(type) {
		case BACKUPCHUNK:
			return executor.submit(new BackUpChunkTask(dbs.getFManager(), chunk));
		case STORECHUNK:
			return executor.submit(new StoreChunkTask(dbs.getFManager(), chunk));
		case RECEIVECHUNK:
			return executor.submit(new ReceiveChunkTask(dbs.getFManager(), chunk));
		default:
			return null;
		}
	}

	synchronized public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo, byte[] body) {
		switch(type) {
		case SENDCHUNK:
			return executor.submit(new SendChunkTask(dbs.getFManager(), fileID, chunkNo, body));
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(TaskTypes type, byte[] fileID, int chunkNo) {
		switch(type) {
		case HANDLE_REMOVE:
			return executor.submit(new HandleRemoveTask(dbs.getFManager(), fileID, chunkNo));
		case DELETE:
			return executor.submit(new DeleteTask(dbs.getFManager(), fileID));
		case RESTORECHUNK:
			return executor.submit(new RestoreChunkTask(dbs.getFManager(), fileID, chunkNo));
		default:
			return null;
		}
	}
	
	synchronized public Future<?> executeTask(Packet packet) {
		if(packet.packetType.equals("PUTCHUNK")) {
			return executor.submit(new StoreChunkTask(dbs.getFManager(), packet.getChunk()));
		}
		else if (packet.packetType.equals("GETCHUNK")) {
			return executor.submit(new SendChunkTask(dbs.getFManager(), packet.getFileID(), packet.getChunkNo(), packet.getData()));
		}
		else if (packet.packetType.equals("DELETE")) {
			return executor.submit(new HandleDeleteTask(dbs.getFManager(), packet.getFileID()));
		}
		//TODO
		//IF A PUTCHUNK TASK IS TAKING PLACE, THIS SHOULD NOT RUN
		else if (packet.packetType.equals("REMOVED")) {
			return executor.submit(new HandleRemoveTask(dbs.getFManager(), packet.getFileID(), packet.getChunkNo()));
		}
		else if (packet.packetType.equals("CHUNK")) {
			return executor.submit(new ReceiveChunkTask(dbs.getFManager(), packet.getChunk()));
		}
		return null;
	}
	
	public void handlePacket(Packet packet) {
		if(packet.packetType.equals("STORED") || packet.packetType.equals("CHUNK")) {
			executor.messageActiveTasks(packet);
		}
		else executeTask(packet);
		//complete this with other messages
	}
	
	public void finish() {
		executor.shutdownNow();
	}
	
	public void sendMessageToActiveTasks(Packet p) {
		executor.messageActiveTasks(p);
	}

	public Future<?> executeTask(TaskTypes type, String name, int repDeg) {
		if(type == TaskTypes.BACKUPFILE) {
			return executor.submit(new BackupFileTask(null, name, repDeg));
		}
		else return null;
			
		
	}
}
