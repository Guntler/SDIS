package pt.up.fe.backup;

//http://stackoverflow.com/questions/1741545/java-calculate-sha-256-hash-of-large-file-efficiently
//http://stackoverflow.com/questions/3103652/hash-string-via-sha-256-in-java

public class BackupFile {
	private byte[] fileID;
	private String filename;
	private int replicationDegree;
	private int chunkNum;
	
	public BackupFile(byte[] hash, String filename, int replicationDegree, int chunkNum) {
		this.fileID = hash;
		this.filename = filename;
		this.replicationDegree = replicationDegree;
		this.chunkNum = chunkNum;
	}

	public String getFilename() {
		return filename;
	}

	public byte[] getFileID() {
		return fileID;
	}
}
