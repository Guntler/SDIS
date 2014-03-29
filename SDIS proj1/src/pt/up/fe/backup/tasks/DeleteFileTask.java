package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

/**
 * Deletes the file from the local system and creates a DeleteTask, which issues a DELETE packet to its peers.
 * @author pbpdi_000
 *
 */
public class DeleteFileTask extends Task {
	byte[] fileID;

	public DeleteFileTask(FileManager fManager, byte[] fileID) {
		super(fManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		FileManager.returnTypes result = fManager.deleteFile(fileID);
		
		if(result != FileManager.returnTypes.FAILURE) {
			result = fManager.deleteAllChunks(fileID);
			
			if(result != FileManager.returnTypes.FAILURE) {
				try {
					Packet pack = new Packet("DELETE", null, fileID, 0, 0, null);
					DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);
					//write info to log
				} catch (IOException e) {e.printStackTrace();}
			}
		}
	}
}