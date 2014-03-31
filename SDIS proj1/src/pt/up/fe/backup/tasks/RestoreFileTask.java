package pt.up.fe.backup.tasks;

import java.util.concurrent.ExecutionException;

import pt.up.fe.backup.BackupFile;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;
import pt.up.fe.backup.TaskManager;

public class RestoreFileTask extends Task{
	String filename;
	
	public RestoreFileTask(FileManager fManager, String filename) {
		super(fManager);
		this.filename = filename;
	}

	@Override
	public void run() {
		boolean done = false;
		BackupFile file = DistributedBackupSystem.fManager.getFileByName(filename);
		int curChunk=0;
		if(file != null) {
			while(!done) {
				try {
					DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.RESTORECHUNK, file.getFileID(), curChunk).get();
				} catch (InterruptedException | ExecutionException e) {e.printStackTrace();}
				curChunk++;
				
				for(Packet p : messages) {
					if(p.getPacketType().equals("DELETE") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(file.getFileID())))
						done = true;
				}
			}
		}
	}
}