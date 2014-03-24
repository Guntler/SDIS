package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class BackUpChunkTask extends Task {
	BackupChunk chunk;
	
	public BackUpChunkTask(FileManager fManager, CommunicationManager cManager, BackupChunk chunk) {
		super(fManager, cManager);
		this.chunk = chunk;
	}
	
	@Override
	public void run() {
		boolean done = false;
		int waitTime = 500;

		try {
			cManager.sendPacket(new Packet("PUTCHUNK", "1.0.0", chunk), CommunicationManager.Channels.MDB);

			do {
				int storedCount = 0;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				for(Packet p : messages) {
					if(p.getPacketType().equals("STORED") && p.getFileID().equals(chunk.getFileID()) && p.getChunkNo() == chunk.getChunkNo()) {
						storedCount++;
					}
				}
				
				if(storedCount >= chunk.getWantedReplicationDegree()) {
					done = true;
					//write info to log
				}
				else if (waitTime < 10000)
					waitTime += waitTime;
				else
					done = true;
			} while (!done);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
