package pt.up.fe.backup;

import java.net.InetAddress;
import java.util.ArrayList;

public class BackupChunk {
	public static int maxSize = 64000;
	
	private byte[] fileID;
	private int chunkNo;
	private byte[] data;
	private String filename;
	private int size;
	private int curReplicationDegree;
	private int wantedReplicationDegree;
	
	private ArrayList<InetAddress> stored;
	
	
	public BackupChunk(byte[] fileID, int chunkNo, byte[] data, String filename, int size, int replicationDegree, int curReplicationDegree, ArrayList<InetAddress> stored) {
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.data = data;
		this.filename = filename;
		this.size = size;
		this.curReplicationDegree = 0;
		this.wantedReplicationDegree = replicationDegree;
		if(stored != null) 
			this.stored = stored;
		else
			this.stored = new ArrayList<InetAddress>();
	}

	public byte[] getFileID() {
		return fileID;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public int getWantedReplicationDegree() {
		return wantedReplicationDegree;
	}

	public byte[] getData() {
		return data;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void incRepDeg(int inc) {
		curReplicationDegree += inc;
	}
	
	public int getRepDeg() {
		return curReplicationDegree;
	}

	public int getSize() {
		return size;
	}

	public ArrayList<InetAddress> getStored() {
		return stored;
	}

	public void addToStored(InetAddress addr) {
		this.stored.add(addr);
	}
	
	public void removeFromStored(InetAddress addr) {
		this.stored.remove(addr);
	}

	public void increaseRepDegree() {
		this.curReplicationDegree++;
	}

	public void decreaseRepDegree() {
		this.curReplicationDegree--;
	}

	public void eraseData() {
		this.data = null;
	}
}
