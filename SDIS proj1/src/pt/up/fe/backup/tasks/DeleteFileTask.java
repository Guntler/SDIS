package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupFile;
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
	String filename;

	public DeleteFileTask(String filename) {
		super();
		this.filename= filename;
	}

	@Override
	public void run() {
		BackupFile file = DistributedBackupSystem.fManager.getFileByName(filename);
		if(file != null) {
			byte[] fileID = file.getFileID();
			FileManager.returnTypes result = DistributedBackupSystem.fManager.deleteFile(fileID);
			if(result != FileManager.returnTypes.FAILURE) {
				result = DistributedBackupSystem.fManager.deleteAllChunks(fileID);
				if(result != FileManager.returnTypes.FAILURE) {
					try {
						Packet pack = new Packet("DELETE", null, fileID, 0, 0, null, null);
						DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);
					} catch (IOException e) {e.printStackTrace();}
				}
			}
		}
	}
}