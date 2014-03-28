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
			Packet pack = new Packet("PUTCHUNK", "1.0.0", chunk);
			DistributedBackupSystem.cManager.sendPacket(pack, CommunicationManager.Channels.MDB);

			do {
				int storedCount = 0;
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {e.printStackTrace();}

				for(Packet p : messages) {
					System.out.println(p.getPacketType() + " Comparing: " + Packet.bytesToHex(p.getFileID()) + " with: " + Packet.bytesToHex(chunk.getFileID()) + " and: " + p.getChunkNo() + " with: " + chunk.getChunkNo());
					if(p.getPacketType().equals("STORED") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(chunk.getFileID())) && p.getChunkNo() == chunk.getChunkNo()) {
						storedCount++;
						System.out.println("this should work");
					}
				}
				
				if(storedCount >= chunk.getWantedReplicationDegree()) {
					System.out.println("Chunk was successfully stored with required replication degree");
					done = true;
					//write info to log
				}
				else if (waitTime < 8000) {
					System.out.println("Timeout: did not receive enough STORED replies");
					waitTime += waitTime;
				}
				else {
					System.out.println("Chunk was not successfully stored");
					done = true;
				}
			} while (!done);
		} catch (IOException e) {e.printStackTrace();}
	}
}
