package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Packet {
	protected ArrayList<String> msgArgs;
	protected String packetType;
	
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
	public Packet(String packetType, String version, byte[] fileID, int chunkNo ) {
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

	public void sendPacket(DatagramSocket socket, String mcastAddr, String mcastPort) throws IOException {
		byte[] buf = new byte[256];
		String msg = new String();
		
		msg += msgArgs.get(0);
		for(int i=1;i<msgArgs.size();i++) {
			msg += " ";
			msg += msgArgs.get(i);
		}
		
		buf = msg.getBytes();
		InetAddress address = InetAddress.getByName(mcastAddr);
		DatagramPacket packet = new DatagramPacket(buf,buf.length,address,Integer.parseInt(mcastPort));
		
		socket.send(packet);
	}
}
