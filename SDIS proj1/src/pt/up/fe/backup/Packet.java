package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.xml.stream.events.Characters;

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
	public Packet(String msg) {
		parseMessage(msg);
	}
	
	/**
	 * Constructor for PutChunk
	 * PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> <CRLF> <Body>
	 * CHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF> <Body>
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * REMOVED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * STORED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * DELETE <FileId> <CRLF>
	 */
	public Packet(String packetType, String version, BackupChunk chunk ) {
		this.packetType = packetType;
	}
	
	public Packet(String packetType, String version, byte[] fileID, int chunkNo, int repDeg, byte[] body ) {
		this.packetType = packetType;
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
		msgArgs.add(packetType);
		if(!version.equals(null))
			msgArgs.add(version);
		if(!fileID.equals(null))
			msgArgs.add(fileID.toString());
		if(chunkNo != 0)
			msgArgs.add(Integer.toString(chunkNo));
		if(replicationDeg != 0)
			msgArgs.add(Integer.toString(replicationDeg));
		//Inserir 0xD 0xA duas vezes
		if(!data.equals(null))
			msgArgs.add(data.toString());
		
		msg += msgArgs.get(0);
		for(int i=1;i<msgArgs.size();i++) {
			msg += " ";
			msg += msgArgs.get(i);
		}
		
		buf = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(buf,buf.length,socket.getInetAddress(),socket.getPort());
		
		socket.send(packet);
	}
	
	public void parseMessage(String msg) {
		if(msg.contains("PUTCHUNK")) {
			String subMsg = msg.substring("PUTCHUNK".length());
			int nextSpace = subMsg.indexOf(' ');
			String version = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String chunkNo = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String repDeg = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(0xA);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(0xA);
			subMsg = subMsg.substring(nextSpace+1);
			String body = subMsg.substring(0,nextSpace);
			this.version = version;
			this.fileID = fileID.getBytes();
			this.chunkNo = Integer.parseInt(chunkNo);
			this.replicationDeg = Integer.parseInt(repDeg);
			this.data = body.getBytes();
		}
		else if(msg.contains("CHUNK")) {
			String subMsg = msg.substring("CHUNK".length());
			int nextSpace = subMsg.indexOf(' ');
			String version = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String chunkNo = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(0xA);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(0xA);
			subMsg = subMsg.substring(nextSpace+1);
			String body = subMsg.substring(0,nextSpace);
			this.version = version;
			this.fileID = fileID.getBytes();
			this.chunkNo = Integer.parseInt(chunkNo);
			this.data = body.getBytes();
		}
		else if(msg.contains("GETCHUNK")) {
			String subMsg = msg.substring("GETCHUNK".length());
			int nextSpace = subMsg.indexOf(' ');
			String version = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String chunkNo = subMsg.substring(0,nextSpace);
			this.version = version;
			this.fileID = fileID.getBytes();
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("REMOVED")) {
			String subMsg = msg.substring("REMOVED".length());
			int nextSpace = subMsg.indexOf(' ');
			String version = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String chunkNo = subMsg.substring(0,nextSpace);
			this.version = version;
			this.fileID = fileID.getBytes();
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("STORED")) {
			String subMsg = msg.substring("STORED".length());
			int nextSpace = subMsg.indexOf(' ');
			String version = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(' ');
			String chunkNo = subMsg.substring(0,nextSpace);
			this.version = version;
			this.fileID = fileID.getBytes();
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("DELETE")) {
			String subMsg = msg.substring("DELETE".length());
			int nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			this.fileID = fileID.getBytes(); 
		}
	}
}
