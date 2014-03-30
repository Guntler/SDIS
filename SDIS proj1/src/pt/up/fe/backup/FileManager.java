package pt.up.fe.backup;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;

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
			    	else if (parts[0].equals("lastchunk:")) {
			    		nextAvailableFileNo = Integer.parseInt(parts[1]);
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
			    	else if (parts[0].equals("chunk:") && parts.length >= 6) {
			    		byte[] hash = Packet.hexToByte(parts[1]);
			    		int chunkNo = Integer.parseInt(parts[3]);
			    		int repDegree = Integer.parseInt(parts[4]);
			    		int curRepDeg = Integer.parseInt(parts[5]);
			    		int size = Integer.parseInt(parts[6]);
			    		
			    		ArrayList<InetAddress> stored = new ArrayList<InetAddress>();
			    		
			    		for(int i = 7; i < parts.length; i++) {
			    			stored.add(InetAddress.getByName(parts[i]));
			    		}
			    		
			    		backedUpChunks.add(new BackupChunk(hash, chunkNo, null, parts[2], size, repDegree, curRepDeg, stored));
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
			
			return partialHash;

		} catch(Exception e) {
			System.out.println("Error reading file: " + e.toString());
		}
		return null;
	}

	synchronized public void backupFile(String filename, int replicationDegree) {
		BackupFile newFile = null;
		byte[] fileHash = computeFileHash(filename);
		
		if(fileHash != null) {
			try {
				byte[] buffer = new byte[BackupChunk.maxSize];
				BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filename));
				int bytesRead = 0;
				int chunkCount = 0;

				while((bytesRead = reader.read(buffer,0,BackupChunk.maxSize)) != -1) {
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, buffer, filename, bytesRead, replicationDegree, 1, null);
					chunkCount++;
					
					dbs.getTManager().executeTask(TaskManager.TaskTypes.BACKUPCHUNK, newChunk).get();
				}
				
				reader.close();
				newFile = new BackupFile(fileHash, filename, replicationDegree, chunkCount);
				files.add(newFile);
				
				String data = "file: " + Packet.bytesToHex(newFile.getFileID()) + " " + newFile.getFilename() + " " + newFile.getReplicationDegree() + " " + chunkCount;

				File file =new File("log.txt");

				if(!file.exists()){
					file.createNewFile();
				}

				FileWriter fileWritter = new FileWriter(file.getName(),true);
				BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
				bufferWritter.write(data);
				bufferWritter.close();

			} catch(Exception e) {
				System.out.println("Error reading file: " + e.toString());
			}
		}
	}
	
	synchronized public returnTypes saveChunk(BackupChunk c) {
		String recID = Packet.bytesToHex(c.getFileID());
		for(BackupChunk chunk : backedUpChunks) {
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID) && chunk.getChunkNo() == c.getChunkNo()) {
				return returnTypes.FILE_EXISTS;
			}
		}
		
		String name = "";
		name += "chunk-";
		name += this.nextAvailableFileNo;

		c.setFilename(name);

		File storeDir = new File("storage");
		storeDir.mkdir();

		try {
			FileOutputStream out = new FileOutputStream("storage/"+name);
			out.write(c.getData());
			out.close();
			this.backedUpChunks.add(c);

			String data = "chunk: " + Packet.bytesToHex(c.getFileID()) + " " + name + " " + c.getChunkNo() + " " + c.getWantedReplicationDegree() + " " + 1 + " " + c.getSize();

			for(InetAddress addr : c.getStored()) {
				data += " " + addr.getHostAddress();
			}

			BufferedReader reader;

			File log = new File("log.txt");

			if(!log.exists())
			{
				PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
				writer.println("allocatedmemory: 1073741824");
				writer.println("usedmemory: 0");
				writer.close();
			}

			reader = new BufferedReader(new FileReader("log.txt"));

			ArrayList<String> fileText = new ArrayList<String>();
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				
				if(parts[0].equals("usedmemory:")) {
					currSize += c.getSize();
					line = "usedmemory: " + this.currSize;
				}
				
				fileText.add(line);
			}
			
			fileText.add(data);
			fileText.add("lastchunk: " + this.nextAvailableFileNo);
			
			reader.close();

			FileWriter fileWritter = new FileWriter(log.getName(),false);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			String towrite = "";
			for(int i = 0; i < fileText.size(); i++) {
				towrite += fileText.get(i);
				if(i < fileText.size()-1)
					towrite += "\n";
			}
			bufferWritter.write(towrite);
			bufferWritter.close();

		} catch (Exception e) {
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
		String recID = Packet.bytesToHex(fileID);
		for(BackupChunk chunk : backedUpChunks) {
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
	 * 
	 * @param fileID
	 * @param chunkNo
	 * @return
	 */
	public BackupChunk getChunk(byte[] fileID, int chunkNo) {
		String recID = Packet.bytesToHex(fileID);
		for(BackupChunk chunk : backedUpChunks) {
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID) && chunk.getChunkNo() == chunkNo) {
				return chunk;
			}
		}
		
		return null;	
	}
	
	/**
	 * Deletes Chunk from FileSystem.
	 * Unfinished. TODO
	 * @param fileID
	 * @return
	 */
	public returnTypes deleteChunk(byte[] fileID, int chunkNo) {
		String recID = Packet.bytesToHex(fileID);
		for(BackupChunk chunk : backedUpChunks) {
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
		for(BackupFile file : files) {
			String comID = Packet.bytesToHex(file.getFileID());
			if(comID.equals(recID)) {
				boolean success = (new File (file.getFilename())).delete();
				if(success) {
					success = files.remove(fileID);
					if(success) {
						return returnTypes.SUCCESS;
					}
				}
			}
		}
		
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
