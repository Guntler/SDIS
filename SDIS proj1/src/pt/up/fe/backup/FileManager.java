package pt.up.fe.backup;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
	public static int bytesToRead = 1048576;
	
	private HashMap<byte[],BackupFile> files;
	private ArrayList<BackupChunk> backedUpChunks;
	private int maxSize, currSize;
	private TaskManager tManager;
	
	public FileManager(TaskManager tManager) {
		files = new HashMap<byte[],BackupFile>();
		backedUpChunks = new ArrayList<BackupChunk>();
		this.tManager = tManager;
	}
	
	public byte[] computeFileHash(String filename) {
		try {
			MessageDigest hashSum = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[bytesToRead];
			byte[] partialHash = null;
			BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filename));
			int bytesRead = 0;

			File file = new File(filename);


			hashSum.update(file.getName().getBytes());
			hashSum.update(ByteBuffer.allocate(8).putLong(file.lastModified()).array());

			while((bytesRead = reader.read(buffer,0,bytesToRead)) != -1) {
				hashSum.update(buffer, 0, bytesRead);
			}

			reader.close();
			partialHash = new byte[hashSum.getDigestLength()];
			partialHash = hashSum.digest();
			
			//BigInteger hash = new BigInteger(1,partialHash);
			
			return partialHash;

		} catch(Exception e) {
			System.err.println("Error reading file: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	public void backupFile(String filename, int replicationDegree) {
		BackupFile newFile = null;
		byte[] fileHash = computeFileHash(filename);
		
		if(fileHash != null) {
			newFile = new BackupFile(fileHash, filename, replicationDegree);
			
			try {
				byte[] buffer = new byte[BackupChunk.maxSize];
				BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filename));
				int bytesRead = 0;
				int chunkCount = 0;

				while((bytesRead = reader.read(buffer,0,BackupChunk.maxSize)) != -1) {
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, buffer, filename, bytesRead);
					
					tManager.
				}

				reader.close();

			} catch(Exception e) {
				System.err.println("Error reading file: " + e.toString());
				e.printStackTrace();
			}
		}
	}
}
