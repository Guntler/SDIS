package pt.up.fe.backup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
	public static int bytesToRead = 1048576;
	
	private HashMap<byte[],BackupFile> files;
	private ArrayList<BackupChunk> backedUpChunks;
	private int maxSize, currSize;
	private DistributedBackupSystem dbs;
	
	public FileManager(DistributedBackupSystem dbs) {
		files = new HashMap<byte[],BackupFile>();
		backedUpChunks = new ArrayList<BackupChunk>();
		this.dbs = dbs;
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("/path/to/file.txt"));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
			    String[] parts = line.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			try {
				byte[] buffer = new byte[BackupChunk.maxSize];
				BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filename));
				int bytesRead = 0;
				int chunkCount = 0;

				while((bytesRead = reader.read(buffer,0,BackupChunk.maxSize)) != -1) {
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, buffer, filename, bytesRead);
					chunkCount++;
					
					dbs.getTManager().executeTask(TaskManager.TaskTypes.BACKUPCHUNK, newChunk).get();
				}
				
				newFile = new BackupFile(fileHash, filename, replicationDegree, chunkCount);

				reader.close();

			} catch(Exception e) {
				System.err.println("Error reading file: " + e.toString());
				e.printStackTrace();
			}
		}
	}
}
