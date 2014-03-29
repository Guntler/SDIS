package pt.up.fe.backup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
	public static int bytesToRead = 1048576;
	public enum returnTypes {
	    SUCCESS, FILE_EXISTS, FILE_DOES_NOT_EXIST, FAILURE
	}
	private ArrayList<BackupFile> files;
	private ArrayList<BackupChunk> backedUpChunks;
	private long maxSize, currSize;
	private DistributedBackupSystem dbs;
	private int nextAvailableFileNo;
	
	public FileManager(DistributedBackupSystem dbs) {
		files = new ArrayList<BackupFile>();
		backedUpChunks = new ArrayList<BackupChunk>();
		this.dbs = dbs;
		this.nextAvailableFileNo = 0;
		
		BufferedReader reader;
		try {
			File log = new File("log.txt");
			
			if(!log.exists())
			{
				PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
				writer.println("allocatedmemory: 1073741824");
				writer.println("usedmemory: 0");
				writer.close();
			}
			
			reader = new BufferedReader(new FileReader("log.txt"));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
			    String[] parts = line.split(" ");
			    
			    if(parts.length >= 2) {
			    	if(parts[0].equals("allocatedmemory:")) {
			    		setMaxSize(Integer.parseInt(parts[1]));
			    	}
			    	else if (parts[0].equals("usedmemory:")) {
			    		setCurrSize(Integer.parseInt(parts[1]));
			    	}
			    	else if (parts[0].equals("file:") && parts.length == 5) {
			    		byte[] hash = Packet.hexToByte(parts[1]);
			    		int repDegree = Integer.parseInt(parts[3]);
			    		int numChunks = Integer.parseInt(parts[4]);
			    		
			    		
			    		files.add(new BackupFile(hash, parts[2], repDegree, numChunks));
			    	}
			    	else if (parts[0].equals("chunk:") && parts.length == 4) {
			    		byte[] hash = Packet.hexToByte(parts[1]);
			    		int chunkNo = Integer.parseInt(parts[2]);
			    		int repDegree = Integer.parseInt(parts[3]);
			    		
			    		backedUpChunks.add(new BackupChunk(hash, chunkNo, null, null, 0, repDegree));
			    	}
			    	
			    }
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
			hashSum.update(Inet4Address.getLocalHost().getHostAddress().getBytes());

			while((bytesRead = reader.read(buffer,0,bytesToRead)) != -1) {
				hashSum.update(buffer, 0, bytesRead);
			}

			reader.close();
			partialHash = new byte[hashSum.getDigestLength()];
			partialHash = hashSum.digest();
			
			//BigInteger hash = new BigInteger(1,partialHash);
			
			return partialHash;

		} catch(Exception e) {
			System.out.println("Error reading file: " + e.toString());
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
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, buffer, filename, bytesRead, replicationDegree);
					chunkCount++;
					
					dbs.getTManager().executeTask(TaskManager.TaskTypes.BACKUPCHUNK, newChunk).get();
				}
				
				newFile = new BackupFile(fileHash, filename, replicationDegree, chunkCount);
				files.add(newFile);

				reader.close();

			} catch(Exception e) {
				System.out.println("Error reading file: " + e.toString());
			}
		}
	}
	
	public returnTypes saveChunk(BackupChunk c) {
		
		for(BackupChunk chunk : backedUpChunks) {
			String recID = Packet.bytesToHex(c.getFileID());
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID) && chunk.getChunkNo() == c.getChunkNo()) {
				return returnTypes.FILE_EXISTS;
			}
		}
		
		String name = "";
		
		name += "chunk-";
		name += this.nextAvailableFileNo;
		name += "-";
		name += c.getChunkNo();
		
		c.setFilename(name);
		
		File storeDir = new File("storage");
		storeDir.mkdir();
		
		try {
			FileOutputStream out = new FileOutputStream("storage/"+name);
			out.write(c.getData());
			out.close();
			this.backedUpChunks.add(c);
		} catch (IOException e) {
			e.printStackTrace();
			return returnTypes.FAILURE;
		}
		
		nextAvailableFileNo++;
		return returnTypes.SUCCESS;
	}

	/**
	 * Deletes all chunks that match with a fileID.
	 * @param fileID
	 * @return
	 */
	public returnTypes deleteAllChunks(byte[] fileID) {
		for(BackupChunk chunk : backedUpChunks) {
			String recID = Packet.bytesToHex(fileID);
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID)) {
				String filepath = "storage/";
				filepath += chunk.getFilename();
				
				boolean success = (new File (filepath)).delete();
				if (success) {
					success = backedUpChunks.remove(chunk);
					
					if(success) {System.out.println("The file has been successfully deleted");}
					else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				}
				else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				
				return returnTypes.SUCCESS;
			}
		}
		
		return returnTypes.FILE_DOES_NOT_EXIST;
	}
	
	/**
	 * Deletes Chunk from FileSystem.
	 * Unfinished. TODO
	 * @param fileID
	 * @return
	 */
	public returnTypes deleteChunk(byte[] fileID, int chunkNo) {
		for(BackupChunk chunk : backedUpChunks) {
			String recID = Packet.bytesToHex(fileID);
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID) && chunk.getChunkNo() == chunkNo) {
				String filepath = "storage/";
				filepath += chunk.getFilename();
				
				boolean success = (new File (filepath)).delete();
				if (success) {
					success = backedUpChunks.remove(chunk);
					
					if(success) {System.out.println("The file has been successfully deleted");}
					else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				}
				else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				
				return returnTypes.SUCCESS;
			}
		}

		return returnTypes.FILE_DOES_NOT_EXIST;
	}
	
	public returnTypes deleteFile(byte[] fileID) {
		String recID = Packet.bytesToHex(fileID);
		BackupFile file = files.remove(fileID);
		
		return returnTypes.FILE_DOES_NOT_EXIST;
	}
	
	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	public long getCurrSize() {
		return currSize;
	}

	public void setCurrSize(long currSize) {
		this.currSize = currSize;
	}
	
	public BackupFile getFileByName(String name) {
		for(BackupFile file : files) {
			if(file.getFilename().equals(name)) {
				return file;
			}
		}
		
		return null;
	}
}
