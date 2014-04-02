package pt.up.fe.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

public class FileManager {
	public static int bytesToRead = 1048576;
	public enum returnTypes {
	    SUCCESS, FILE_EXISTS, FILE_DOES_NOT_EXIST, FAILURE, WAIT
	}
	private CopyOnWriteArrayList<BackupFile> files;
	private CopyOnWriteArrayList<BackupChunk> backedUpChunks;
	private long maxSize, currSize;
	private DistributedBackupSystem dbs;
	private int nextAvailableFileNo;
	
	public FileManager(DistributedBackupSystem dbs) {
		files = new CopyOnWriteArrayList<BackupFile>();
		backedUpChunks = new CopyOnWriteArrayList<BackupChunk>();
		this.dbs = dbs;
		
		readLog();
	}
	
	synchronized public void updateLog() {

		try {
			PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
			writer.println("allocatedmemory: " + this.maxSize);
			writer.println("usedmemory: " + this.currSize);
			writer.println("lastchunk: " + this.nextAvailableFileNo);
			for(BackupFile file : files) {
				writer.println("file: " + Packet.bytesToHex(file.getFileID()) + " " + file.getFilename() + " " + file.getReplicationDegree() + " " + file.getNumChunks());
			}
			for(BackupChunk chunk : backedUpChunks) {
				String chunkData = "chunk: " + Packet.bytesToHex(chunk.getFileID()) + " " + chunk.getFilename() + " " + chunk.getChunkNo() + " " + chunk.getWantedReplicationDegree() + " " + chunk.getRepDeg() + " " + chunk.getSize();

				for(InetAddress addr : chunk.getStored()) {
					chunkData += " " + addr.getHostAddress();
				}
				writer.println(chunkData);
			}
			writer.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	synchronized public void readLog() {
		BufferedReader reader;
		try {
			File log = new File("log.txt");
			
			if(!log.exists())
			{
				PrintWriter writer = new PrintWriter("log.txt", "UTF-8");
				writer.println("allocatedmemory: 1073741824");
				writer.println("usedmemory: 0");
				writer.println("lastchunk: 0");
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
			
			reader.close();
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

	public void backupFile(String filename, int replicationDegree) {
		BackupFile newFile = null;
		byte[] fileHash = computeFileHash(filename);
		
		if(fileHash != null) {
			for(BackupFile file : files) {
				if(Packet.bytesToHex(file.getFileID()).equals(Packet.bytesToHex(fileHash))) {
					System.out.println("File already exists in system");
					return;
				}
			}
			try {
				byte[] buffer = new byte[BackupChunk.maxSize];
				BufferedInputStream reader = new BufferedInputStream(new FileInputStream(filename));
				int bytesRead = 0;
				int prevBytesRead = 0;
				int chunkCount = 0;

				while((bytesRead = reader.read(buffer,0,BackupChunk.maxSize)) != -1) {
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, Arrays.copyOfRange(buffer, 0, bytesRead), filename, bytesRead, replicationDegree, 1, null);
					chunkCount++;
					dbs.getTManager().executeTask(TaskManager.TaskTypes.BACKUPCHUNK, newChunk).get();
					prevBytesRead = bytesRead;
				}
				reader.close();
				
				if(prevBytesRead == BackupChunk.maxSize) {
					BackupChunk newChunk = new BackupChunk(fileHash, chunkCount, null, filename, 0, replicationDegree, 1, null);
					chunkCount++;
					dbs.getTManager().executeTask(TaskManager.TaskTypes.BACKUPCHUNK, newChunk).get();
				}
				
				newFile = new BackupFile(fileHash, filename, replicationDegree, chunkCount);
				files.add(newFile);
				updateLog();

			} catch(Exception e) {
				System.out.println("Error reading file: " + e.toString());
			}
		}
	}
	
	public returnTypes saveChunk(BackupChunk c) {
		String recID = Packet.bytesToHex(c.getFileID());
		for(BackupChunk chunk : backedUpChunks) {
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID) && chunk.getChunkNo() == c.getChunkNo()) {
				return returnTypes.FILE_EXISTS;
			}
		}
		
		if(currSize + c.getSize() > maxSize) {
			try {
				DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.REMOVE, null).get();
			} catch (InterruptedException e) {e.printStackTrace();} catch (ExecutionException e) {e.printStackTrace();}
			
			if(currSize + c.getSize() > maxSize)
				return returnTypes.FAILURE;
		}

		synchronized(this) {
			String name = "";
			name += "chunk-";
			name += this.nextAvailableFileNo;

			c.setFilename(name);

			File storeDir = new File("storage");
			storeDir.mkdir();

			try {
				FileOutputStream out = new FileOutputStream("storage/"+name);
				if(c.getData() != null)
					out.write(c.getData());
				out.close();
				c.eraseData();
				c.setCurrRepDeg(1);
				this.backedUpChunks.add(c);
				this.currSize += c.getSize();
				this.nextAvailableFileNo++;
				updateLog();

			} catch (Exception e) {
				e.printStackTrace();
				return returnTypes.FAILURE;
			}
		}

		return returnTypes.SUCCESS;
	}

	/**
	 * Deletes all chunks that match with a fileID.
	 * @param fileID
	 * @return
	 */
	public returnTypes deleteAllChunks(byte[] fileID) {
		String recID = Packet.bytesToHex(fileID);
		boolean found = false;
		for(BackupChunk chunk : backedUpChunks) {
			String comID = Packet.bytesToHex(chunk.getFileID());
			if(comID.equals(recID)) {
				String filepath = "storage/";
				filepath += chunk.getFilename();
				
				boolean success = (new File (filepath)).delete();
				if (success) {
					currSize -= chunk.getSize();
					success = backedUpChunks.remove(chunk);
					
					if(success) {System.out.println("The file has been successfully deleted");updateLog();found = true;}
					else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				}
				else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				
				
			}
		}
		
		if(found)
			return returnTypes.SUCCESS;
		else
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
				byte[] body = new byte[BackupChunk.maxSize];

				try {
					@SuppressWarnings("resource")
					BufferedInputStream reader = new BufferedInputStream(new FileInputStream("storage/" + chunk.getFilename()));
					int bytesRead = 0;

					bytesRead = reader.read(body,0,BackupChunk.maxSize);
					byte[] arr = null;
					if(bytesRead > 0)
						arr = Arrays.copyOfRange(body, 0, bytesRead);
					
					return new BackupChunk(chunk.getFileID(), chunk.getChunkNo(), arr, chunk.getFilename(), bytesRead, chunk.getWantedReplicationDegree(), chunk.getRepDeg(), null);
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				} 
			}
		}
		
		return null;	
	}
	
	/**
	 * Deletes Chunk from FileSystem.
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
					currSize -= chunk.getSize();
					success = backedUpChunks.remove(chunk);
					
					if(success) {System.out.println("The file has been successfully deleted");updateLog();}
					else {System.out.println("Error occurred while deleting the file."); return returnTypes.FAILURE;}
				}
				else {System.out.println("Error occurred while deleting the file"); return returnTypes.FAILURE;}
				
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
				new File (file.getFilename()).delete();
				boolean success = files.remove(file);
				if(success) {
					updateLog();
					return returnTypes.SUCCESS;
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

	synchronized public void updateRepDegree(byte[] fileID, int chunkNo, InetAddress addr, boolean removeOrAdd) {
		for(BackupChunk chunk : this.backedUpChunks) {
			if(Packet.bytesToHex(chunk.getFileID()).equals(Packet.bytesToHex(fileID)) && chunk.getChunkNo() == chunkNo) {
				boolean alreadyStored = false;
				for(InetAddress a : chunk.getStored())
				{
					if(a.equals(addr)) {
						alreadyStored = true;
						
						if(!removeOrAdd)
							chunk.getStored().remove(a);
						break;
					}
				}
				
				if(removeOrAdd && !alreadyStored) {
					chunk.increaseRepDegree();
					chunk.addToStored(addr);
				}
				else if (alreadyStored){
					chunk.decreaseRepDegree();
				}

				updateLog();
				return;
			}
		}
	}
	
	public boolean chunkCorrectRepDegree(byte[] fileID, int chunkNo) {
		for(BackupChunk chunk : backedUpChunks) {
			if(Packet.bytesToHex(chunk.getFileID()).equals(Packet.bytesToHex(fileID)) && chunkNo == chunk.getChunkNo()) {
				if(chunk.getRepDeg() < chunk.getWantedReplicationDegree()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public void releaseSpace() {
		while(currSize > maxSize) {
			BackupChunk maxRepDeg = null;
			
			for(BackupChunk chunk : backedUpChunks) {
				if(maxRepDeg == null || chunk.getRepDeg() > maxRepDeg.getRepDeg())
					maxRepDeg = chunk;
			}
			
			if(deleteChunk(maxRepDeg.getFileID(), maxRepDeg.getChunkNo()) == returnTypes.SUCCESS) {
				try {
					DistributedBackupSystem.cManager.sendPacket(new Packet("REMOVED", "1.0", maxRepDeg.getFileID(), maxRepDeg.getChunkNo(), 0, null, null), CommunicationManager.Channels.MC);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public returnTypes writeChunk(BackupChunk chunk) {
		String recID = Packet.bytesToHex(chunk.getFileID());
		for(BackupFile file : files) {
			String comID = Packet.bytesToHex(file.getFileID());
			if(comID.equals(recID)) {
				File restoredFile = new File(file.getFilename());
				if(!restoredFile.exists())
					try {restoredFile.createNewFile();} catch (IOException e) {e.printStackTrace();}
				
				try {
					BufferedOutputStream buffOut=new BufferedOutputStream(new FileOutputStream(restoredFile, true));
					buffOut.write(chunk.getData());
					buffOut.flush();
					buffOut.close();
					updateLog();
					return returnTypes.SUCCESS;
				} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return returnTypes.FAILURE;
	}
	
	public void printAllFiles() {
		for(BackupFile file : files) {
			file.printAllInfo();
		}
	}

	public CopyOnWriteArrayList<BackupFile> getFiles() {
		return files;
	}

	public void setFiles(CopyOnWriteArrayList<BackupFile> files) {
		this.files = files;
	}
}
