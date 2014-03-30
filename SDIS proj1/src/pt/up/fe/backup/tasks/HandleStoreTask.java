package pt.up.fe.backup.tasks;

import java.net.InetAddress;

import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.FileManager;


public class HandleStoreTask extends Task implements Runnable {
	private byte[] fileID;
	private int chunkNo;
	private InetAddress addr;

	public HandleStoreTask(FileManager fManager, byte[] fileID, int chunkNo, InetAddress addr) {
		super(fManager);
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.addr = addr;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.updateRepDegree(fileID, chunkNo, addr, true);
	}

}
