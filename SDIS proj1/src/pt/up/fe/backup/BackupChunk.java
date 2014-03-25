package pt.up.fe.backup;

public class BackupChunk {
	public static int maxSize = 65536;
	
	private byte[] fileID;
	private int chunkNo;
	private byte[] data;
	private String filename;
	private int size;
	private int curReplicationDegree;
	private int wantedReplicationDegree;
	
	public BackupChunk(byte[] fileID, int chunkNo, byte[] data, String filename, int size, int replicationDegree) {
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.data = data;
		this.setFilename(filename);
		this.size = size;
		this.curReplicationDegree = 0;
		this.wantedReplicationDegree = replicationDegree;
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
}
