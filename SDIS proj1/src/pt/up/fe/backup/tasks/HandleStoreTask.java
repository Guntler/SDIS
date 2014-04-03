package pt.up.fe.backup.tasks;

import java.net.InetAddress;

import pt.up.fe.backup.DistributedBackupSystem;


public class HandleStoreTask extends Task implements Runnable {
	private byte[] fileID;
	private int chunkNo;
	private InetAddress addr;

	public HandleStoreTask(byte[] fileID, int chunkNo, InetAddress addr) {
		super();
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.addr = addr;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.updateRepDegree(fileID, chunkNo, addr, true);
	}

	@Override
	public String toString() {
		return "handlestore";
	}
}
