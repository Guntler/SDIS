package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class RestoreChunkTask extends Task {
	byte[] fileID;
	int chunkNo;

	public RestoreChunkTask(FileManager fManager, byte[] fileID, int chunkNo) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		boolean done = false;
		int waitTime = 500;

		try {
			Packet pack = new Packet("GETCHUNK", "1.0", fileID, chunkNo, 0, null);
			DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MC);

			do {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {e.printStackTrace();}

				for(Packet p : messages) {
					if(p.getPacketType().equals("CHUNK") && p.getFileID().equals(this.fileID) && p.getChunkNo() == this.chunkNo) {
						BackupChunk chunk = p.getChunk();
						fManager.saveChunk(chunk);
						done = true;
						//write info to log
					}
				}

				if (waitTime < 10000)
					waitTime += waitTime;
				else
					done = true;
			} while (!done);
		} catch (IOException e) {e.printStackTrace();}
	}

}
