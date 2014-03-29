package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

/**
 * Takes place when the User issues a REMOVED packet. Deletes the chunk from memory and from the file system,
 * then sends a REMOVED message to the channels.
 * @author pbpdi_000
 *
 */
public class RemoveTask extends Task {
	byte[] fileID;
	int chunkNo;

	public RemoveTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		FileManager.returnTypes result = fManager.deleteChunk(fileID,chunkNo);
		//write info to log
		
		if(result == FileManager.returnTypes.SUCCESS) {
			try {
				Packet pack = new Packet("REMOVED", "1.0", fileID, chunkNo, 0, null);
				DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);
				//write info to log
			} catch (IOException e) {e.printStackTrace();}
		}
	}
}
