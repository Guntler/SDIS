package pt.up.fe.backup;

public class BackupChunk {
	public static int maxSize = 65536;
	
	private byte[] fileID;
	private int chunkNo;
	private byte[] data;
	private String filename;
	private int size;
	
	public BackupChunk(byte[] fileID, int chunkNo, byte[] data, String filename, int size) {
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.data = data;
		this.filename = filename;
		this.size = size;
	}
}
