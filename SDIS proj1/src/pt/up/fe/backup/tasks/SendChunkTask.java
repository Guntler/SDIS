package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class SendChunkTask extends Task {
	byte[] fileID;
	int chunkNo;
	byte[] body;

	public SendChunkTask(FileManager fManager, byte[] fileID, int chunkNo, byte[] body) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
	}

	@Override
	public void run() {
		boolean done = false;
		int waitTime = 500;

		try {
			try {
				Thread.sleep(waitTime);
				
				for(Packet p : messages) {
					if(p.getPacketType().equals("CHUNK") && p.getFileID().equals(this.fileID) && p.getChunkNo() == this.chunkNo) {
						done = true;
						//write info to log ????
					}
				}
				
				if(!done) {
					Packet pack = new Packet("CHUNK", "1.0", fileID, chunkNo, 0, body);
					DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MDR);
					//write info to log
				}
			} catch (InterruptedException e) {e.printStackTrace();}
		} catch (IOException e) {e.printStackTrace();}
	}

}
