package pt.up.fe.backup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Packet {
	protected ArrayList<String> msgArgs = new ArrayList<String>();
	protected String packetType;
	protected byte[] fileID;
	protected int chunkNo;
	protected int replicationDeg;
	protected String version;
	protected byte[] data;
	
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
		this.version = version;
		this.chunkNo = chunk.getChunkNo();
		this.replicationDeg = chunk.getWantedReplicationDegree();
		this.data = chunk.getData();
		this.fileID = chunk.getFileID();
	}
	
	public Packet(String packetType, String version, byte[] fileID, int chunkNo, int repDeg, byte[] body ) {
		this.packetType = packetType;
		this.version = version;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDeg = repDeg;
		this.data = body;
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

	public void sendPacket(DBSsocket socket) throws IOException {
		byte[] buf = new byte[256];
		String msg = new String();
		msgArgs.add(packetType);
		if(!version.equals(null))
			msgArgs.add(version);
		if(!fileID.equals(null)) 
			msgArgs.add(bytesToHex(fileID));
		
		if(chunkNo != -1)
			msgArgs.add(Integer.toString(chunkNo));
		if(replicationDeg != 0)
			msgArgs.add(Integer.toString(replicationDeg));
	
		msgArgs.add(new String(new byte[] {0xA}));
		msgArgs.add(new String(new byte[] {0xD}));
		if(!packetType.equals("DELETE")) {
			msgArgs.add(new String(new byte[] {0xA}));
			msgArgs.add(new String(new byte[] {0xD}));
		}
		if(!data.equals(null)) {
			String str = new String(data,StandardCharsets.ISO_8859_1);
			msgArgs.add(str);
		}
		
		msg += msgArgs.get(0);
		for(int i=1;i<msgArgs.size();i++) {
			msg += " ";
			msg += msgArgs.get(i);
		}

		buf = msg.getBytes();
		
		DatagramPacket packet = new DatagramPacket(buf,buf.length,socket.getMcastGroup(),socket.getLocalPort());
		
		socket.send(packet);
	}
	
	public static String bytesToHex(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}
	
	public static byte[] hexToByte(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	public void parseMessage(String msg) {
		String cr = new String(new byte[] {0xA});
		String lf = new String(new byte[] {0xD});
		String crlf = cr+lf;
		
		if(msg.contains("PUTCHUNK")) {
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3];
			String repDeg = packOptions[4].split("\\r?\\n")[0];
			String body = packOptions[4].split("\\r?\\n")[2];
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			System.out.println(this.fileID);
			this.version = version;
			this.packetType = "PUTCHUNK";
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
			nextSpace = subMsg.indexOf(crlf);
			subMsg = subMsg.substring(nextSpace+1);
			nextSpace = subMsg.indexOf(crlf);
			subMsg = subMsg.substring(nextSpace+1);
			String body = subMsg.substring(0,nextSpace);
			this.version = version;
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.chunkNo = Integer.parseInt(chunkNo);
			try {this.data = body.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
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
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
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
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
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
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("DELETE")) {
			String subMsg = msg.substring("DELETE".length());
			int nextSpace = subMsg.indexOf(' ');
			String fileID = subMsg.substring(0,nextSpace);
			try {this.fileID = fileID.getBytes("ISO-8859-1");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
		}
	}

	public BackupChunk getChunk() {
		return new BackupChunk(fileID, chunkNo, data, null, data.length,replicationDeg);
	}
}
