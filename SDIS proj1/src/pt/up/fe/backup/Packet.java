package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Packet {
	protected ArrayList<String> msgArgs;
	protected String packetType;
	protected byte[] fileID;
	protected int chunkNo;
	protected int replicationDeg;
	protected String version;
	protected byte[] data;
	protected BackupChunk chunk;
	
	/**
	 * @param args	<MessageType> <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 */
	public Packet() {
	}
	
	/**
	 * Constructor for PutChunk
	 * PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> <CRLF> <Body>
	 */
	public Packet(String version, int replicationDeg, BackupChunk chunk ) {
		packetType = "PUTCHUNK";
	}

	/**
	 * Constructor for GetChunk and Removed
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * REMOVED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 */
	public Packet(String packetType, int version, byte[] fileID, int chunkNo ) {
		this.packetType = packetType;
	}

	/**
	 * Constructor for Delete
	 * DELETE <FileId> <CRLF>
	 */
	public Packet(int filedId) {
		packetType = "DELETE";
	}
	
	/**
	 * Constructor for Stored
	 * STORED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 */
	public Packet(String version, byte[] fileID, int chunkNo) {
		packetType = "STORED";
	}
	
	/**
	 * Constructor for Chunk
	 * CHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF> <Body>
	 */
	public Packet(String version, BackupChunk chunk) {
		packetType = "CHUNK";
	}
	
	public BackupChunk getChunk() {
		return chunk;
	}

	public void setChunk(BackupChunk chunk) {
		this.chunk = chunk;
	}

	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getPacketType() {
		return packetType;
	}

	public void setPacketType(String packetType) {
		this.packetType = packetType;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public byte[] getFileID() {
		return fileID;
	}

	public void setFileID(byte[] fileID) {
		this.fileID = fileID;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}


	public void sendPacket(DatagramSocket socket) throws IOException {
		byte[] buf = new byte[256];
		String msg = new String();
		
		msg += msgArgs.get(0);
		for(int i=1;i<msgArgs.size();i++) {
			msg += " ";
			msg += msgArgs.get(i);
		}
		
		buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf,buf.length,socket.getInetAddress(),socket.getPort());
		
		socket.send(packet);
	}
}
