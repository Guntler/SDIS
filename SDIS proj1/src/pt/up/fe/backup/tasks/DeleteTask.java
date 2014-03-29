package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

/**
 * Issues a DELETE packet to its peers.
 * @author pbpdi_000
 *
 */
public class DeleteTask extends Task {
	byte[] fileID;

	public DeleteTask(FileManager fManager, byte[] fileID) {
		super(fManager);
		this.fileID = fileID;
	}

	@Override
	public void run() {
		try {
			Packet pack = new Packet("DELETE", null, fileID, 0, 0, null);
			DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);
			//write info to log
		} catch (IOException e) {e.printStackTrace();}	
	}
}