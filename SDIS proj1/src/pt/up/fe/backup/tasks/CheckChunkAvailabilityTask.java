package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.BackupFile;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.Packet;

public class CheckChunkAvailabilityTask extends Task {

	public CheckChunkAvailabilityTask() {
		super();
		
	}

	@Override
	public void run() {
		for(BackupFile file : DistributedBackupSystem.fManager.getFiles()) {
			boolean exists = false;
			Packet pack = new Packet("GETCHUNK", "1.0", new BackupChunk(file.getFileID(), 0, null, file.getFilename(), 0, file.getReplicationDegree(), 0, null), null);
			try {
				DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MDB);
			} catch (IOException e) {e.printStackTrace();}
			
			long waitTime = 500;
			while(waitTime < 1000) {
				for(Packet p : messages) {
					if(p.getPacketType().equals("CHUNK") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(file.getFileID())) && p.getChunkNo() == 0) {
						exists = true;
						break;
					}
				}
				
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {e.printStackTrace();}
				
				if(exists)
					break;
				else
					waitTime+=waitTime;
			}
			
			if(!exists) {
				DistributedBackupSystem.tManager.sendMessageToActiveTasks(new Packet("DELETE", "1.0", file.getFileID(), -1, 0, null, null));
			}
		}
		
	}

}
