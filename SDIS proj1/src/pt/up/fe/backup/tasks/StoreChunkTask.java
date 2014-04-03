package pt.up.fe.backup.tasks;

import java.io.IOException;

import pt.up.fe.backup.BackupChunk;
import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;
import pt.up.fe.backup.FileManager.returnTypes;

public class StoreChunkTask extends Task {
	private BackupChunk chunk;

	public StoreChunkTask(BackupChunk chunk) {
		super();
		this.chunk = chunk;
	}

	@Override
	public void run() {

		int chance = 3;
		int backupChance =(int) (Math.random() * chance);

		while(backupChance != 0 && chance > 0) {
			messages.clear();

			int waitTime = (int)(Math.random() * 400);
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e1) {e1.printStackTrace();}
			int numStored = 0;
			for(Packet p : messages) {
				if(p.getPacketType().equals("STORED") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(chunk.getFileID())) && p.getChunkNo() == chunk.getChunkNo()) {
					numStored++;
				}
			}

			if(numStored >= chunk.getWantedReplicationDegree())
				return;
			else
				chance--;
			
			backupChance = (int) (Math.random() * chance);
		}
		
		FileManager.returnTypes result = DistributedBackupSystem.fManager.saveChunk(chunk);
		
		if(result != FileManager.returnTypes.FAILURE) {
			
			int waitTime = (int)(Math.random() * 400);
			try {
				Thread.sleep(waitTime);
			} catch (InterruptedException e1) {e1.printStackTrace();}
		
			try {
				DistributedBackupSystem.cManager.sendPacket(new Packet("STORED", "1.0", chunk.getFileID(), chunk.getChunkNo(), 0, null, null), CommunicationManager.Channels.MC);
			} catch (IOException e) {e.printStackTrace();}
		}
	}

}
	