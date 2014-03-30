package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public class BackUpChunkTask extends Task {
	BackupChunk chunk;
	
	public BackUpChunkTask(FileManager fManager, BackupChunk chunk) {
		super(fManager);
		this.chunk = chunk;
	}
	
	@Override
	public void run() {
		boolean done = false;
		int waitTime = 500;

		try {
			Packet pack = new Packet("PUTCHUNK", "1.0", chunk, null);
			DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MDB);

			do {
				int storedCount = 0;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {e.printStackTrace();}

				for(Packet p : messages) {
					if(p.getPacketType().equals("STORED") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(chunk.getFileID())) && p.getChunkNo() == chunk.getChunkNo()) {
						storedCount++;
					}
				}
				
				if(storedCount >= chunk.getWantedReplicationDegree()) {
					System.out.println("Chunk was successfully stored with required replication degree");
					done = true;
				}
				else if (waitTime < 8000) {
					System.out.println("Timeout: did not receive enough STORED replies");
					waitTime += waitTime;
				}
				else {
					System.out.println("Chunk was not successfully stored");
					DistributedBackupSystem.tManager.sendMessageToActiveTasks(new Packet("DELETE", "1.0", chunk.getFileID(), 0, 0, null, null));
					done = true;
				}
				
				messages.clear();
			} while (!done);
		} catch (IOException e) {e.printStackTrace();}
	}

	@Override
	public String toString() {
		return "backupchunk";
	}
}
