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
		
		try {
			cManager.sendPacket(new Packet("PUTCHUNK", "1.0.0", chunk), CommunicationManager.Channels.MDB);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
