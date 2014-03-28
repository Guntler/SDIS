package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class HandleRemoveTask extends Task {
	byte[] fileID;
	int chunkNo;		//not initialized/used

	public HandleRemoveTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		boolean result = fManager.deleteChunk(fileID);
		//write info to log
		
		if(result) {
			try {
				Packet pack = new Packet("REMOVED", "1.0", fileID, chunkNo, 0, null);
				DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);
				//write info to log
			} catch (IOException e) {e.printStackTrace();}
		}
	}
}
